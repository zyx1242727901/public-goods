package com.xh.publicgoods.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserInvestVO {
    private String userName;
    private Boolean invested;
}
