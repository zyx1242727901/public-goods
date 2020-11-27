package com.xh.publicgoods.constants;

public class RedisConstants {
    //===========================  有效期相关  ====================================
    public static final Integer ONE_HOUR = 60*60;
    public static final Integer FIVE_MINUTE = 60*5;
    public static final int ONE_DAY = 60 * 60 * 24;




    //===========================  房间相关  ====================================
    /**
     * 当前房间已进入玩家  占位符-房间号
     */
    public static final String ROOM_USER_SET = "room_%s_set";
    /**
     * 房间账户
     */
    public static final String ROOM_ACCOUNT = "room_%s_account";
    /**
     * 房间号set
     */
    public static final String ROOM_ID_SET = "room_id_set";



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
    /**
     * 用户注册信息集合
     */
    public static final String USER_INFO_HASH = "user_info_hash";
    /**
     * 用户情绪  username_房间号
     */
    public static final String USER_MOOD_STRING = "user_mood_%s";


    //===========================  房间内游戏动态相关  ====================================
    /**
     * 某房间用户操作记录   占位符-房间号
     */
    public static final String INVEST_OPERATE_RECORD = "invest_%s_operate_record";
    /**
     * 当前房间某回合已投资人数  占位符-房间号
     */
    public static final String ROOM_INVEST_USER_COUNT = "invest_%s_count";
    /**
     * 是否清算标记
     */
    public static final String ROOM_ROUND_LIQUIDATION_FLAG = "%s_%s_liquidation_flag";
    /**
     * 是否销毁房间标记
     */
    public static final String ROOM_FINALIZE_FLAG = "%s_finalize_flag";



    //===========================  锁相关  ====================================
    public static final String LOCK_ROOM = "lock_%s_room";
    public static final String LOCK_ROOM_LIQUIDATION = "lock_%s_room_liquidation";


}
