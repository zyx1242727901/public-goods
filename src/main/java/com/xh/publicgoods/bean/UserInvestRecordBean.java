package com.xh.publicgoods.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
public class UserInvestRecordBean {
    private String userName;
    private BigDecimal amount;
}
