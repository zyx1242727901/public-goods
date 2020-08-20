package com.xh.publicgoods.schedule;

import com.xh.publicgoods.constants.RedisConstants;
import com.xh.publicgoods.util.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublicGoodsTask {
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 用户信息删除，解决用户名重复问题
     */
    @Scheduled(cron = "0 0 0 1/2 * ?")
    public void initUserInfo(){
        log.info("============PublicGoodsTask.initUserInfo enter============");
        redisHelper.del(RedisConstants.USER_INFO_HASH);
        log.info("============PublicGoodsTask.initUserInfo end============");
    }
}
