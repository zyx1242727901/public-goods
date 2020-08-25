package com.xh.publicgoods.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserInvestRecordBean {
    private String userName;
    private BigDecimal amount;
    private Integer timeScan;
}
