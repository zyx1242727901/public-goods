package com.xh.publicgoods.constants;

public class RedisConstants {
    //===========================  房间相关  ====================================
    /**
     * 当前房间已进入玩家数  占位符-房间号
     */
    public static final String ROOM_USER_COUNT = "room_%s_count";



    //===========================  用户相关  ====================================
    /**
     * 用户账户变动记录  占位符-userName
     */
    public static final String USER_ACCOUNT_CHANG_RECORD = "user_%s_account_chang_record";
    /**
     * 用户账户总金额  占位符-userName
     */
    public static final String USER_ACCOUNT_SUM_MONEY = "user_%s_account_sum_money";
    /**
     * 已存在用户名
     */
    public static final String USER_NAME_SET = "user_name_set";



    //===========================  房间内游戏动态相关  ====================================
    /**
     * 某房间用户操作记录   占位符-房间号
     */
    public static final String INVEST_OPERATE_RECORD = "invest_%s_operate_record";
    /**
     * 当前房间某回合已投资人数  占位符-房间号
     */
    public static final String ROOM_INVEST_USER_COUNT = "invest_%s_count";

}
