package com.xh.publicgoods.controller;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.enums.ResultEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暂时都用requestMapping，免得凯锅页面请求方式写起来麻烦
 * @author zhangyuxiao
 */
@RestController
@RequestMapping("/index")
public class IndexController {

    /**
     * 游戏大厅页面数据
     */
    @RequestMapping("/getHallInfo")
    public JSONObject getHallInfo(){
        return ResultEnum.returnResultJson(ResultEnum.SUCCESS);
    }


}
