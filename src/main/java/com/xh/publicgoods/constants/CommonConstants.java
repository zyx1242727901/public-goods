package com.xh.publicgoods.constants;

import java.math.BigDecimal;

public class CommonConstants {
    /**
     * 每个房间最大用户数
     */
    public static final Long ROOM_USER_MAX_COUNT = 4L;
    /**
     * 用户每回合初始金额
     */
    public static final BigDecimal USER_ROUND_INIT_MONEY = new BigDecimal("100");
    /**
     * 操作记录集合
     * #{0}:userName
     * #{1}:投资数
     */
    public static final String ROUND_INVEST_RECOURD = "%s,%s";
    /**
     * 金额清算阈值
     */
    public static final BigDecimal MONEY_THRESHOLD = new BigDecimal("200");


}
