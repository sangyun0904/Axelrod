package com.sykim.axelrod.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String REDIS_HOST; // Redis 호스트 주소
    @Value("${spring.redis.port}")
    private int REDIS_PORT;    // Redis 포트 번호


    @Bean
    public JedisPool getRedisPool() {
        return new JedisPool(REDIS_HOST, REDIS_PORT);
    }
}
