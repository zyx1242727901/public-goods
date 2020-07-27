package com.xh.publicgoods.enums;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.constants.ResultConstants;
import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS("成功"),
    FAIL("失败"),
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
