package com.xh.publicgoods.controller;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.enums.GenderEnum;
import com.xh.publicgoods.enums.ResultEnum;
import com.xh.publicgoods.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 暂时都用requestMapping，免得凯锅页面请求方式写起来麻烦
 * @author zhangyuxiao
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    /**
     * 用户注册
     */
    @RequestMapping("/{userName}/{age}/{gender}/login")
    public JSONObject getHallInfo(@PathVariable(value = "userName") String userName,
                                  @PathVariable(value = "age")Integer age,
                                  @PathVariable(value = "gender")GenderEnum gender){
        return userService.login(userName, age, gender);
    }

    /**
     * 用户进入房间
     */
    @RequestMapping("/{userName}/{roomId}/enterRoom")
    public JSONObject enterRoom(@PathVariable(value = "userName") String userName,
                                @PathVariable(value = "roomId")String roomId){
        return userService.enterRoom(userName, roomId);
    }

    /**
     * 用户投资
     */
    @RequestMapping("/{userName}/{round}/{roomId}/{amount}/userInvest")
    public JSONObject enterRoom(@PathVariable(value = "userName") String userName,
                                @PathVariable(value = "round") Long round,
                                @PathVariable(value = "roomId") String roomId,
                                @PathVariable(value = "amount")String amount){
        return userService.userInvest(userName, round, roomId, new BigDecimal(amount));
    }

    /**
     * 回合清算
     */
    @RequestMapping("/{round}/{roomId}/liquidation")
    public JSONObject enterRoom(@PathVariable(value = "round") Long round,
                                @PathVariable(value = "roomId")String roomId){
        return userService.liquidation(round, roomId);
    }


}
