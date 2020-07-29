package com.xh.publicgoods.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
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
        Map<String, Long> resMap = new HashMap<>();

        //遍历房间set
        Set<String> smembers = redisHelper.smembers(RedisConstants.ROOM_ID_SET);

        if (!CollectionUtils.isEmpty(smembers)) {
            smembers.stream().forEach(roomId->{
                //获取每个房间的人员数
                Long userCount = redisHelper.scard(String.format(RedisConstants.ROOM_USER_SET, roomId));
                resMap.put(roomId, userCount);
            });
        }

        json.put("resMap", resMap);
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
        //清楚房间内人员
        redisHelper.del(String.format(RedisConstants.ROOM_USER_SET, roomId));
        //房间总账户
        redisHelper.del(String.format(RedisConstants.ROOM_ACCOUNT, roomId));
        //房间已投人数
        redisHelper.del(String.format(RedisConstants.ROOM_INVEST_USER_COUNT, roomId));
        //清楚用户操作数据集合
        redisHelper.del(String.format(RedisConstants.INVEST_OPERATE_RECORD, roomId));

        Set<String> smembers = redisHelper.smembers(String.format(RedisConstants.ROOM_USER_SET, roomId));
        if (!CollectionUtils.isEmpty(smembers)) {
            smembers.stream().forEach(userName -> {
                //清楚用户账户变动分红记录
                redisHelper.del(String.format(RedisConstants.USER_ACCOUNT_CHANG_RECORD, userName));
                //清楚账户总额
                redisHelper.del(String.format(RedisConstants.USER_ACCOUNT_SUM_MONEY, userName));
            });
        }

        return json;
    }

    public JSONObject finalizeGame(String roomId){
        //导出文件
        outPutDataToExcel(roomId);
        //关闭房间
        this.destroyRoom(roomId);

        return ResultEnum.returnResultJson(ResultEnum.SUCCESS);
    }


    public void outPutDataToExcel(String roomId){
        try {
            // 1. 使用File类打开一个文件；
            String filePath =new StringBuffer(".").append(File.separator).append("data_").append(roomId).append(".xlsx").toString();
            EasyExcel.write(filePath, DataModel.class).sheet("sheet2").doWrite(packageData(roomId));
        } catch (Exception e) {
            log.error("outPutDataToTxt ERROR",e);
            throw new RuntimeException(e);
        }
    }

    private List<DataModel> packageData(String roomId){
        List<DataModel> list = new ArrayList<>();
        //获取用户集合
        Set<String> users = redisHelper.smembers(String.format(RedisConstants.ROOM_USER_SET, roomId));
        if (!CollectionUtils.isEmpty(users)) {

            users.stream().forEach(userName -> {
                DataModel temp = new DataModel();
                temp.setUserName(userName);

            });
        }
//        for (int i = 0; i < 10; i++) {
//
//            temp.setUserName("zhangsan");
//            temp.setRound(2L);
//            temp.setInvestMoney(BigDecimal.ONE);
//            temp.setBounsMoney(BigDecimal.TEN);
//            temp.setSumMoney(new BigDecimal("100.02"));
//            list.add(temp);
//        }
        return list;

    }

    public static void main(String[] args) {
        new RoomServiceImpl().outPutDataToExcel("321");
    }
}
