package com.klzw.common.redis;

import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Redis测试应用启动类
 */
@SpringBootApplication(exclude = {RedissonAutoConfigurationV2.class})
public class MineguardCommonRedisApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(MineguardCommonRedisApplication.class, args);
    }
}
