package com.xh.publicgoods.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author zhangyuxiao
 */
@Service
@Slf4j
public class RedisService {
    @Autowired
    private JedisPool jedisPool;

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
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
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
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
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
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }


    public Long hset(String key,String secondKey, String value) {
        Jedis jedis = jedisPool.getResource();
        try {
            Long set = jedis.hset(key, secondKey, value);
            return set;
        } catch (Exception e) {
            log.error("RedisService.hset Error key=" + key + " value=" + value, e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Long hdel(String key, final byte[]... fields) {
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.hdel(key.getBytes(), fields);
        } catch (Exception e) {
            log.error("RedisService.hdel Error" + key, e);
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
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public Long sadd(String key,String... members) {
        Jedis jedis = jedisPool.getResource();
        try {
            Long set = jedis.sadd(key, members);
            return set;
        } catch (Exception e) {
            log.error("RedisService.sadd Error key=" + key + " members=" + members, e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Set<String> smembers(String key) {
        Set<String> result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.smembers(key);
        } catch (Exception e) {
            log.error("RedisService.smembers Error key=" + key, e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
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
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
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
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

}
