package com.xh.publicgoods.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE("男"),
    FE_MALE("女"),
    ;

    private String desc;

    GenderEnum(String desc) {
        this.desc = desc;
    }

}
