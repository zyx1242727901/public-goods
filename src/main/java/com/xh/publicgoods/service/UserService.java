package com.xh.publicgoods.service;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.bean.MoodDTO;
import com.xh.publicgoods.enums.GenderEnum;

import java.math.BigDecimal;

public interface UserService {
    /**
     * 进入房间事件
     * @param userName
     * @param roomId
     * @return
     */
    JSONObject enterRoom(String userName, String roomId);
    /**
     * 投资接口
     * @param userName
     * @param round
     * @param roomId
     * @param amount
     * @return
     */
    JSONObject userInvest(String userName, Long round, String roomId, BigDecimal amount, Integer timeScan);
    /**
     * 清算接口
     * @param round
     * @param roomId
     * @return
     */
    JSONObject liquidation(Long round, String roomId, String userName) throws InterruptedException;

    JSONObject login(String userName, Integer age, GenderEnum gender);

    JSONObject enterMood(String userName, MoodDTO moodDTO);
}
