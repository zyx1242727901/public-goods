package com.xh.publicgoods.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.bean.HallInfoVO;
import com.xh.publicgoods.bean.UserInvestRecordBean;
import com.xh.publicgoods.constants.CommonConstants;
import com.xh.publicgoods.constants.RedisConstants;
import com.xh.publicgoods.enums.ResultEnum;
import com.xh.publicgoods.excel.DataModel;
import com.xh.publicgoods.service.RoomService;
import com.xh.publicgoods.util.GenerateUtil;
import com.xh.publicgoods.util.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 游戏大厅页面数据
     */
    @Override
    public JSONObject getHallInfo() {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);
        List<HallInfoVO> list = new ArrayList<>();

        //遍历房间set
        Set<String> smembers = redisHelper.smembers(RedisConstants.ROOM_ID_SET);

        if (!CollectionUtils.isEmpty(smembers)) {
            smembers.stream().forEach(roomId->{
                //获取每个房间的人员数
                Long userCount = redisHelper.scard(String.format(RedisConstants.ROOM_USER_SET, roomId));
                list.add(new HallInfoVO(roomId, userCount));
            });
        }

        json.put("resMap", list);
        return json;
    }

    /**
     * 创建房间
     * @return
     */
    @Override
    public JSONObject createRoom() {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);
        String roomId = GenerateUtil.generate();
        redisHelper.sadd(RedisConstants.ROOM_ID_SET,roomId);
        json.put("roomId", roomId);
        return json;
    }

    /**
     * 销毁房间
     * @return
     */
    @Override
    public JSONObject destroyRoom(String roomId) {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        //房间总账户
        redisHelper.del(String.format(RedisConstants.ROOM_ACCOUNT, roomId));
        //房间已投人数
        redisHelper.del(String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId));
        //清楚用户操作数据集合
        redisHelper.del(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId));
        //房间大厅集合删除
        redisHelper.srem(RedisConstants.ROOM_ID_SET, roomId);

        Set<String> smembers = redisHelper.smembers(String.format(RedisConstants.ROOM_USER_SET, roomId));
        if (!CollectionUtils.isEmpty(smembers)) {
            smembers.stream().forEach(userName -> {
                //清楚用户账户变动分红记录
                redisHelper.del(String.format(RedisConstants.USER_ACCOUNT_CHANG_RECORD, userName));
                //清楚账户总额
                redisHelper.del(String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName));
            });
        }
        //清楚房间内人员
        redisHelper.del(String.format(RedisConstants.ROOM_USER_SET, roomId));

        return json;
    }

    /**
     * 结束游戏
     * @param roomId
     * @return
     */
    @Override
    public JSONObject finalizeGame(String roomId){
        if (redisHelper.setnx(String.format(RedisConstants.ROOM_FINALIZE_FLAG, roomId), "true", RedisConstants.ONE_HOUR) == 1) {
            //导出文件
            outPutDataToExcel(roomId);
            //关闭房间
            this.destroyRoom(roomId);
        }

        return ResultEnum.returnResultJson(ResultEnum.SUCCESS);
    }

    @Override
    public JSONObject queryStartFlag(String roomId) {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);
        Long scard = redisHelper.scard(String.format(RedisConstants.ROOM_USER_SET, roomId));
        if (CommonConstants.ROOM_USER_MAX_COUNT.compareTo(scard) > 0) {
            json.put("startFlag", false);
        } else {
            json.put("startFlag", true);
        }
        return json;
    }

    @Override
    public JSONObject queryFullInvestFlag(String roomId, Long round) {
        JSONObject json = ResultEnum.returnResultJson(ResultEnum.SUCCESS);

        String count = redisHelper.get(String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId));
        Boolean flag = Long.parseLong(StringUtils.isEmpty(count) ? "0" : count) >= CommonConstants.ROOM_USER_MAX_COUNT;
        //查询已投人数
        String investRecord = redisHelper.hget(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId), round.toString());
        List<UserInvestRecordBean> recordBeans = null;
        if (!StringUtils.isEmpty(investRecord)) {
            recordBeans = JSONArray.parseArray(investRecord, UserInvestRecordBean.class);
        }

        json.put("investUser", recordBeans);
        json.put("fullFlag", flag);
        //返回房间账户总额
        if (flag) {
            String moneyKey = String.format(RedisConstants.ROOM_ACCOUNT, roomId);
            json.put("roomAccountMoney", redisHelper.get(moneyKey));
        }

        return json;
    }


    public void outPutDataToExcel(String roomId){
        try {
            // 1. 使用File类打开一个文件；
            StringBuffer parentPath = new StringBuffer(".").append(File.separator).append("excel").append(File.separator);
            File parentFile = new File(parentPath.toString());
            if (!parentFile.exists()) {
                parentFile.mkdir();
            }
            String filePath = parentPath.append("data_").append(roomId).append(".xlsx").toString();

            EasyExcel.write(filePath, DataModel.class).sheet("public-goods").doWrite(packageData(roomId));
        } catch (Exception e) {
            log.error("outPutDataToTxt ERROR",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 组装
     * @param roomId
     * @return
     */
    private List<DataModel> packageData(String roomId){
        List<DataModel> list = new ArrayList<>();
        //获取用户集合
        Map<String, String> record = redisHelper.hmgetAll(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId));
        if (!CollectionUtils.isEmpty(record)) {
            record.entrySet().stream().forEach(entry -> {
                List<UserInvestRecordBean> bean = JSONArray.parseArray(entry.getValue(), UserInvestRecordBean.class);
                bean.stream().forEach(investRecord -> {
                    DataModel temp = new DataModel();
                    temp.setUserName(investRecord.getUserName());
                    temp.setRound(Long.parseLong(entry.getKey()));
                    temp.setInvestMoney(investRecord.getAmount());
                    String bouns = redisHelper.hget(String.format(RedisConstants.USER_ACCOUNT_CHANG_RECORD,investRecord.getUserName()), entry.getKey());
                    temp.setBounsMoney(CommonConstants.USER_ROUND_INIT_MONEY.subtract(temp.getInvestMoney()).add(new BigDecimal(StringUtils.isEmpty(bouns) ? "0" : bouns)));
                    temp.setSumMoney(new BigDecimal(redisHelper.get(String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY,temp.getUserName()))));
                    temp.setGender(JSON.parseObject(redisHelper.hget(RedisConstants.USER_INFO_HASH,temp.getUserName())).getString("gender"));
                    list.add(temp);
                });
            });
        }

        return list;
    }

    public static void main(String[] args) {
        new RoomServiceImpl().outPutDataToExcel("321");
    }
}
