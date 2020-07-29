package com.xh.publicgoods.bean;

import com.xh.publicgoods.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class UserInfo {
    private String userName;
    private Integer age;
    /**
     * 性别
     */
    private GenderEnum gender;
}
