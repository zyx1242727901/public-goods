package com.xh.publicgoods.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.bean.UserInfo;
import com.xh.publicgoods.bean.UserInvestRecordBean;
import com.xh.publicgoods.constants.CommonConstants;
import com.xh.publicgoods.constants.RedisConstants;
import com.xh.publicgoods.enums.GenderEnum;
import com.xh.publicgoods.enums.ResultEnum;
import com.xh.publicgoods.service.UserService;
import com.xh.publicgoods.util.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public JSONObject enterRoom(String userName, String roomId) {
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(roomId)) {
            return ResultEnum.returnResultJson(ResultEnum.E0000002);
        }
        String key = String.format(RedisConstants.ROOM_USER_SET, roomId);

        if (CommonConstants.ROOM_USER_MAX_COUNT.compareTo(redisHelper.scard(key)) <= 0) {
            return ResultEnum.returnResultJson(ResultEnum.E0000004);
        }

        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        redisHelper.sadd(key,userName);

        return json;
    }

    /**
     * 投资接口
     * @param userName
     * @param round
     * @param roomId
     * @param amount
     * @return
     */
    @Override
    public JSONObject userInvest(String userName, Long round, String roomId, BigDecimal amount, Integer timeScan) {
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(roomId) || round == null || amount == null || timeScan == null) {
            return ResultEnum.returnResultJson(ResultEnum.E0000002);
        }
        if (amount.compareTo(CommonConstants.USER_ROUND_INIT_MONEY) > 0) {
            return ResultEnum.returnResultJson(ResultEnum.E0000006);
        }
        //校验是否当前回合已投过
        String investRecord = redisHelper.hget(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId), round.toString());
        if (!StringUtils.isEmpty(investRecord) && investRecord.contains('\"'+userName+'\"')) {
            return ResultEnum.returnResultJson(ResultEnum.E0000005);
        }

        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        UserInvestRecordBean currentRecord = new UserInvestRecordBean(userName,amount,timeScan);
        String firstKey = String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId);
        String secondKey = round.toString();

        RLock lock = redisHelper.getLock(String.format(RedisConstants.LOCK_ROOM, roomId));
        Jedis jedis = redisHelper.getInstance();
        try {
            lock.lock();
            Transaction multi = jedis.multi();

            //用户投资行为保存
            String record = redisHelper.hget(firstKey, secondKey);
            JSONArray array = StringUtils.isEmpty(record) ? new JSONArray() : JSONArray.parseArray(record);
            array.add(currentRecord);
            redisHelper.multiHset(multi, firstKey, secondKey, array.toJSONString());
            //账户金额变更
            incrUserMoneyByInvest(amount, userName, multi);
            //房间账户增加
            redisHelper.multiIncr(multi, String.format(RedisConstants.ROOM_ACCOUNT, roomId), amount.longValue());
            //房间已投人数变更
            redisHelper.multiIncr(multi, String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId), 1L);
            redisHelper.exec(multi);

        } catch (Exception e) {
            log.error("UserServiceImpl.userInvest ERROR params::" + userName + "," + roomId + "," + round + "," + amount, e);
            return ResultEnum.returnResultJson(ResultEnum.FAIL);
        }finally {
            lock.unlock();
            jedis.close();
        }


        return json;
    }

    /**
     * 清算接口
     * @param round
     * @param roomId
     * @return
     */
    @Override
    public JSONObject liquidation(Long round, String roomId, String userName) throws InterruptedException {
        if (StringUtils.isEmpty(roomId) || round == null) {
            return ResultEnum.returnResultJson(ResultEnum.E0000002);
        }
        String flagKey = String.format(RedisConstants.ROOM_ROUND_LIQUIDATION_FLAG, roomId, round.toString());

        if (!StringUtils.isEmpty(redisHelper.get(flagKey))) {
            return getLiquidationRes(roomId, round, userName);
        }
        RLock lock = redisHelper.getLock(String.format(RedisConstants.LOCK_ROOM_LIQUIDATION, roomId));
        if (lock.tryLock()) {
            Jedis jedis = redisHelper.getInstance();
            try {
                Transaction multi = jedis.multi();
                //计算分红并保存
                liquidationBonus(roomId, round, multi);
                //房间已投人数清零
                redisHelper.multiSetex(multi, String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId), "0", RedisConstants.ONE_HOUR);

                //房间账户清零
                redisHelper.multiSetex(multi, String.format(RedisConstants.ROOM_ACCOUNT, roomId), "0", RedisConstants.ONE_HOUR);
                redisHelper.exec(multi);
                //设置清算结束标志位
                redisHelper.setex(flagKey, "true", RedisConstants.ONE_DAY);

                return getLiquidationRes(roomId, round, userName);
            } catch (Exception e) {
                log.error("UserServiceImpl.liquidation ERROR params::" + roomId + "," + round, e);
                return ResultEnum.returnResultJson(ResultEnum.FAIL);
            } finally {
                lock.unlock();
                jedis.close();
            }
        } else {
            //说明已经开始清算了，此时睡几秒直接查询即可，无需加锁
            while (true) {
                TimeUnit.SECONDS.sleep(2);
                if (!StringUtils.isEmpty(redisHelper.get(flagKey))){
                    return liquidation(round, roomId, userName);
                }
            }
        }

    }

    private JSONObject getLiquidationRes(String roomId, Long round, String userName) {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);
        String bonus = redisHelper.hget(String.format(RedisConstants.USER_ACCOUNT_CHANG_RECORD, userName), round.toString());
        if (StringUtils.isEmpty(bonus)) {
            return ResultEnum.returnResultJson(ResultEnum.FAIL);
        }
        //分红
        json.put("bonus", bonus);
        //账户总金额
        String currentAmt = redisHelper.get(String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName));
        json.put("sumAmt", currentAmt);
        //投资金额
        String record = redisHelper.hget(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId),round.toString());
        List<UserInvestRecordBean> userInvestRecordBeans = JSONArray.parseArray(record, UserInvestRecordBean.class);
        if (!CollectionUtils.isEmpty(userInvestRecordBeans)) {
            userInvestRecordBeans.stream().forEach(investRecord -> {
                if (userName.equals(investRecord.getUserName())) {
                    json.put("investAmt", investRecord.getAmount());
                    return;
                }
            });
        }

        return json;
    }

    @Override
    public JSONObject login(String userName, Integer age, GenderEnum gender) {
        if (redisHelper.hexists(RedisConstants.USER_INFO_HASH, userName)) {
            return ResultEnum.returnResultJson(ResultEnum.E0000003);
        }

        redisHelper.hset(RedisConstants.USER_INFO_HASH, userName, JSONObject.toJSONString(new UserInfo(userName, age, gender)));
        JSONObject jsonObject = ResultEnum.returnResultJson(ResultEnum.SUCCESS);
        jsonObject.put("userName", userName);
        return jsonObject;
    }

    private void incrUserMoneyByInvest(BigDecimal amount, String userName, Transaction multi) {
        String accountKey = String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName);

        String moneyStr = redisHelper.get(accountKey);
        BigDecimal userCurrentMoney = new BigDecimal(StringUtils.isEmpty(moneyStr) ? "0" : moneyStr).add(CommonConstants.USER_ROUND_INIT_MONEY.subtract(amount));
        redisHelper.multiSetex(multi, accountKey, userCurrentMoney.toPlainString(), RedisConstants.ONE_HOUR);
    }

    private void incrUserMoneyByLiquidation(BigDecimal amount, String userName, Transaction multi) {
        String accountKey = String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName);

        String moneyStr = redisHelper.get(accountKey);
        BigDecimal userCurrentMoney = new BigDecimal(StringUtils.isEmpty(moneyStr) ? "0" : moneyStr).add(amount);
        redisHelper.multiSetex(multi,accountKey, userCurrentMoney.toPlainString(), RedisConstants.ONE_HOUR);
    }



    private void liquidationBonus(String roomId, Long round, Transaction multi) {
        String roomAccount = redisHelper.get(String.format(RedisConstants.ROOM_ACCOUNT, roomId));
        if (StringUtils.isEmpty(roomAccount)) {
            throw new RuntimeException("房间账户金额为空:" + roomId);
        }
        BigDecimal roomSumMoney = new BigDecimal(roomAccount);
        BigDecimal bonusTemp = BigDecimal.ZERO;
        if (CommonConstants.MONEY_THRESHOLD.compareTo(roomSumMoney) <= 0) {
            //分红金额
            bonusTemp  = roomSumMoney.multiply(new BigDecimal(2)).divide(new BigDecimal(CommonConstants.ROOM_USER_MAX_COUNT), 2, RoundingMode.HALF_UP);
        }

        final BigDecimal bonus = bonusTemp;
        //获取投资人
        Set<String> users = redisHelper.smembers(String.format(RedisConstants.ROOM_USER_SET, roomId));
        if (!CollectionUtils.isEmpty(users)) {
            //用户总钱数增加
            users.stream().forEach(userName->{
                incrUserMoneyByLiquidation(bonus,userName,multi);
                redisHelper.multiHset(multi, String.format(RedisConstants.USER_ACCOUNT_CHANG_RECORD, userName), round.toString(), bonus.toPlainString());
            });
        }
    }



}
