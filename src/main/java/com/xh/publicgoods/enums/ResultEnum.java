package com.xh.publicgoods.enums;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.constants.ResultConstants;
import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS("成功"),
    FAIL("抱歉，服务器繁忙，请稍后重试"),

    E0000001("请求过于频繁，请稍后再试"),
    E0000002("参数缺失"),
    E0000003("用户名已存在"),
    E0000004("房间人数已满"),
    E0000005("当前回合操作已完成，请等待下一回合操作"),
    E0000006("投资金额不合法"),
    ;


    private String desc;

    ResultEnum(String desc) {
        this.desc = desc;
    }

    public static JSONObject returnResultJson(ResultEnum resultEnum){
        JSONObject jsonObject = new JSONObject();
        return returnResultJson(resultEnum, jsonObject);
    }

    public static JSONObject returnResultJson(ResultEnum resultEnum, JSONObject jsonObject){
        jsonObject.put(ResultConstants.RES_CODE, resultEnum.name());
        jsonObject.put(ResultConstants.RES_MSG, resultEnum.getDesc());
        return jsonObject;
    }
}
