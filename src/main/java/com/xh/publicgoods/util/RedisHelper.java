package com.xh.publicgoods.util;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author zhangyuxiao
 */
@Component
@Slf4j
public class RedisHelper {
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 不覆盖旧值
     * 0：失败  1：成功
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public Long setnx(String key, String value, int expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            Long result = jedis.setnx(key, value);
            if (result == 1 && expire > 0) {
                jedis.expire(key, expire);
            }
            jedis.expire(key, expire);
            return result;
        } catch (Exception e) {
            log.error("RedisService.setnx Error key=" + key + " value=" + value + " expire=" + expire, e);
            throw new RuntimeException(e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 如果该key已存在，则覆盖旧值
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public String setex(String key, String value, int expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            String set = jedis.setex(key, expire, value);
            return set;
        } catch (Exception e) {
            log.error("RedisService.setex Error key=" + key + " value=" + value + " expire=" + expire, e);
            throw new RuntimeException(e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String multiSetex(Transaction transaction, String key, String value, int expire) {
        try {
            Response<String> setex = transaction.setex(key, expire, value);
            return setex.get();
        } catch (Exception e) {
            log.error("RedisService.multiSetex Error key=" + key + " value=" + value + " expire=" + expire, e);
            try {
                transaction.close();
            } catch (IOException ex) {
                log.error("RedisService.multiSetex close transaction ERROR", ex);
            }
            throw new RuntimeException(e);
        }
    }


    public String get(String key) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.get(key);
            return result;
        } catch (Exception e) {
            log.error("RedisService.get Error key=" + key , e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String multiGet(Transaction transaction, String key) {

        try {
            Response<String> result = transaction.get(key);
            return result.get();
        } catch (Exception e) {
            log.error("RedisService.multiGet Error key=" + key , e);
            try {
                transaction.close();
            } catch (IOException ex) {
                log.error("RedisService.multiGet close transaction ERROR", ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Long incr(String key,Long offset) {
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.incrBy(key,offset);
        } catch (Exception e) {
            log.error("RedisService incr exception by key" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public Long multiIncr(Transaction transaction, String key, Long offset) {
        try {

            return transaction.incrBy(key, offset).get();
        } catch (Exception e) {
            log.error("RedisService multiIncr exception by key" + key, e);
            try {
                transaction.close();
            } catch (IOException ex) {
                log.error("RedisService.multiIncr close transaction ERROR", ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Long del(String key) {
        Jedis jedis = jedisPool.getResource();

        try {
            Long del = jedis.del(key);
            return del;
        } catch (Exception e) {
            log.error("RedisService.del Error key=" + key , e);
            throw new RuntimeException(e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long hset(String key,String secondKey, String value) {
        Jedis jedis = jedisPool.getResource();
        try {
            Long set = jedis.hset(key, secondKey, value);
            return set;
        } catch (Exception e) {
            log.error("RedisService.hset Error key=" + key + " value=" + value, e);
            throw new RuntimeException(e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long multiHset(Transaction transaction, String key,String secondKey, String value) {
        try {
            Response<Long> setex = transaction.hset(key, secondKey, value);
            return setex.get();
        } catch (Exception e) {
            log.error("RedisService.multiHset Error key=" + key + " value=" + value + " expire=" + secondKey, e);
            try {
                transaction.close();
            } catch (IOException ex) {
                log.error("RedisService.multiHset close transaction ERROR", ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Long hdel(String key, final byte[]... fields) {
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.hdel(key.getBytes(), fields);
        } catch (Exception e) {
            log.error("RedisService.hdel Error" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public List<String> hmget(String key, String... fields) {
        List<String> result = new ArrayList<>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.hmget(key, fields);
        } catch (Exception e) {
            log.error("RedisService.hmget Error" + key + " field="+fields, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public String hget(String key, String field) {
        String value = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.hget(key, field);
        } catch (Exception e) {
            log.error("RedisService.hget exception by key" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }

    public Boolean hexists(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hexists(key, field);
        } catch (Exception e) {
            log.error("RedisService.hexists exception by key" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long sadd(String key,String... members) {
        Jedis jedis = jedisPool.getResource();
        try {
            Long set = jedis.sadd(key, members);
            return set;
        } catch (Exception e) {
            log.error("RedisService.sadd Error key=" + key + " members=" + members, e);
            throw new RuntimeException(e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long multiSadd(Transaction transaction, String key,String... members) {
        try {
            Response<Long> setex = transaction.sadd(key, members);
            return setex.get();
        } catch (Exception e) {
            log.error("RedisService.multiSadd Error key=" + key + " members=" + members, e);
            try {
                transaction.close();
            } catch (IOException ex) {
                log.error("RedisService.multiSadd close transaction ERROR", ex);
            }
            throw new RuntimeException(e);
        }
    }

    public Set<String> smembers(String key) {
        Set<String> result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.smembers(key);
        } catch (Exception e) {
            log.error("RedisService.smembers Error key=" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    /**
     * 获取set中元素个数
     * @param key
     * @return
     */
    public Long scard(String key) {
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.scard(key);
        } catch (Exception e) {
            log.error("RedisService.scard exception by key" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
            ;
        }
        return result;
    }


    /**
     * 判断成员元素value是否是集合key的成员
     *
     * @param key
     * @param value
     * @return
     */
    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Boolean sismember = jedis.sismember(key, value);
            return sismember;
        } catch (Exception e) {
            log.error("RedisService.sismember Error key=" + key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long expire(String key, int seconds) {
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.expire(key, seconds);
            return result;
        } catch (Exception e) {
            log.error("RedisService.expire Error key=" + key + " seconds=" + seconds, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Transaction multi(){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Transaction multi = jedis.multi();
            return multi;
        } catch (Exception e) {
            log.error("RedisService.multi Error", e);
            throw new RuntimeException(e);
        } finally {
//            if (jedis != null) {
//                jedis.close();
//            }
        }
    }

    public void exec(Transaction transaction){
        if (transaction == null) {
            return ;
        }
        try {
            List<Object> exec = transaction.exec();
            System.out.println("11111  "+JSONArray.toJSONString(exec));
        } catch (Exception e) {
            log.error("RedisService.exec Error", e);
            transaction.discard();
            throw new RuntimeException(e);
        } finally {
            if (transaction != null) {
                try {
                    transaction.close();
                } catch (IOException e) {
                    log.error("RedisService.exec close transaction ERROR", e);
                }
            }
        }
    }


    public RLock getLock(String key){
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redissonClient.getLock(key);
    }
}
