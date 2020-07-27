package com.xh.publicgoods.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author zhangyuxiao
 */
@Configuration
public class RedisConfig {
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private Integer redisPort;
    @Value("${redis.auth}")
    private String redisPwd;
    @Value("${redis.timeout}")
    private Integer timeout;

    @Value("${spring.redis.jedis.pool.max-active}")
    private Integer maxActive;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private Integer maxIdle;
    @Value("${spring.redis.jedis.pool.max-wait}")
    private Long maxWait;
    @Value("${spring.redis.jedis.pool.min-idle}")
    private Integer minIdle;


    @Bean
    public JedisPool getPoolInstans(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMaxTotal(maxActive);
        JedisPool jedisPool = new JedisPool(poolConfig, redisHost, redisPort, timeout, redisPwd);
        return jedisPool;
    }

}
