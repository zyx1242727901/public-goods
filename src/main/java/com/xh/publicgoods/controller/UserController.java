package com.xh.publicgoods.controller;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.enums.GenderEnum;
import com.xh.publicgoods.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暂时都用requestMapping，免得凯锅页面请求方式写起来麻烦
 * @author zhangyuxiao
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    /**
     * 用户注册
     */
    @RequestMapping("/{userName}/{age}/{gender}/login")
    public JSONObject getHallInfo(@PathVariable(value = "userName") String userName,
                                  @PathVariable(value = "age")Integer age,
                                  @PathVariable(value = "gender")GenderEnum gender){
        log.info(userName + "  " + age + "  " + gender);
        return ResultEnum.returnResultJson(ResultEnum.SUCCESS);
    }
}
