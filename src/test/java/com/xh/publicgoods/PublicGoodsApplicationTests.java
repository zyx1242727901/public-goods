package com.xh.publicgoods;

import com.xh.publicgoods.constants.RedisConstants;
import com.xh.publicgoods.util.RedisHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PublicGoodsApplicationTests {

    @Autowired
    private RedisHelper redisHelper;

    @Test
    public void testRedisLock() {
        AtomicInteger integer = new AtomicInteger(1);
        int roundCout=10;
        AtomicInteger cout = new AtomicInteger(roundCout);

        for (int i = 0; i < roundCout; i++) {
            new Thread(() -> {
                RLock lock = redisHelper.getLock(String.format(RedisConstants.LOCK_ROOM_LIQUIDATION, 1));
                try {
                    lock.lock();
                        integer.incrementAndGet();
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cout.decrementAndGet();

                    if (lock.isLocked()) {
                        lock.unlock();
                    }
                }
            }).start();
        }
        while (cout.get() > 0) {

            continue;
        }
        System.out.println(integer);
    }

}
