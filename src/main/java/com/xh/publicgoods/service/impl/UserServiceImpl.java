package com.xh.publicgoods.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.bean.UserInfo;
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
import redis.clients.jedis.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

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

        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        String key = String.format(RedisConstants.ROOM_USER_SET, roomId);
        redisHelper.sadd(key,userName);
        Long scard = redisHelper.scard(key);
        if (CommonConstants.ROOM_USER_MAX_COUNT.compareTo(scard) > 0) {
            json.put("startFlag", false);
        } else {
            json.put("startFlag", true);
        }

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
    public JSONObject userInvest(String userName, Long round, String roomId, BigDecimal amount) {
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(roomId) || round == null || amount == null) {
            return ResultEnum.returnResultJson(ResultEnum.E0000002);
        }
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        String currentRecord = String.format(CommonConstants.ROUND_INVEST_RECOURD, userName, amount);
        String firstKey = String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId);
        String secondKey = round.toString();


        RLock lock = redisHelper.getLock(String.format(RedisConstants.LOCK_ROOM, roomId));
        if(lock.tryLock()){
            try {
                Transaction multi = redisHelper.multi();

                //用户投资行为保存
                String record = redisHelper.hget(firstKey, secondKey);
                record = record == null ? currentRecord : record + currentRecord;
                redisHelper.multiHset(multi, firstKey, secondKey, record);
                //账户金额变更
                incrUserMoneyByInvest(amount, userName, multi);
                //房间账户增加
                redisHelper.multiIncr(multi, String.format(RedisConstants.ROOM_ACCOUNT, roomId), amount.longValue());
                //房间已投人数变更
                Long incr = redisHelper.multiIncr(multi, String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId), 1L);
                json.put("fullGlag", incr < CommonConstants.ROOM_USER_MAX_COUNT);
                redisHelper.exec(multi);
            } catch (Exception e) {
                log.error("UserServiceImpl.userInvest ERROR params::" + userName + "," + roomId + "," + round + "," + amount, e);
                return ResultEnum.returnResultJson(ResultEnum.FAIL);
            }finally {
                lock.unlock();
            }
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
    public JSONObject liquidation(Long round, String roomId) {
        if (StringUtils.isEmpty(roomId) || round == null) {
            return ResultEnum.returnResultJson(ResultEnum.E0000002);
        }
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        RLock lock = redisHelper.getLock(String.format(RedisConstants.LOCK_ROOM, roomId));
        if(lock.tryLock()){
            try {
                Transaction multi = redisHelper.multi();
                //计算分红并保存
                liquidationBonus(roomId, round, multi);
                //房间已投人数清零
                redisHelper.multiSetex(multi, String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId), "0", RedisConstants.ONE_HOUR);

                //房间账户清零
                redisHelper.multiSetex(multi, String.format(RedisConstants.ROOM_ACCOUNT, roomId), "0", RedisConstants.ONE_HOUR);
                redisHelper.exec(multi);
            } catch (Exception e) {
                log.error("UserServiceImpl.liquidation ERROR params::" + roomId + "," + round, e);
                return ResultEnum.returnResultJson(ResultEnum.FAIL);
            }finally {
                lock.unlock();
            }
        }

        return json;
    }

    @Override
    public JSONObject login(String userName, Integer age, GenderEnum gender) {
        if (redisHelper.hexists(RedisConstants.USER_INFO_HASH, userName)) {
            return ResultEnum.returnResultJson(ResultEnum.E0000003);
        }

        redisHelper.hset(RedisConstants.USER_INFO_HASH, userName, JSONObject.toJSONString(new UserInfo(userName, age, gender)));
        return ResultEnum.returnResultJson(ResultEnum.SUCCESS);
    }

    private void incrUserMoneyByInvest(BigDecimal amount, String userName, Transaction multi) {
        String accountKey = String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName);

        String moneyStr = redisHelper.multiGet(multi, accountKey);
        BigDecimal userCurrentMoney = new BigDecimal(moneyStr).add(CommonConstants.USER_ROUND_INIT_MONEY.subtract(amount));
        redisHelper.multiSetex(multi, accountKey, userCurrentMoney.toPlainString(), RedisConstants.ONE_HOUR);
    }

    private void incrUserMoneyByLiquidation(BigDecimal amount, String userName, Transaction multi) {
        String accountKey = String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName);

        String moneyStr = redisHelper.multiGet(multi, accountKey);
        BigDecimal userCurrentMoney = new BigDecimal(moneyStr).add(amount);
        redisHelper.multiSetex(multi,accountKey, userCurrentMoney.toPlainString(), RedisConstants.ONE_HOUR);
    }



    private void liquidationBonus(String roomId, Long round, Transaction multi) {
        String roomAccount = redisHelper.get(String.format(RedisConstants.ROOM_ACCOUNT, roomId));
        if (StringUtils.isEmpty(roomAccount)) {
            throw new RuntimeException("房间账户金额为空:" + roomId);
        }
        BigDecimal roomSumMoney = new BigDecimal(roomAccount);
        if (CommonConstants.MONEY_THRESHOLD.compareTo(roomSumMoney) <= 0) {
            //分红金额
            BigDecimal bonus = roomSumMoney.multiply(new BigDecimal(2)).divide(new BigDecimal(CommonConstants.ROOM_USER_MAX_COUNT), 2, RoundingMode.HALF_UP);
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

}
