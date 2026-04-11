package com.klzw.common.database;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {"com.klzw.common.database", "com.klzw.common.core"},
    excludeName = {
        // Redis 相关自动配置
        "com.klzw.common.auth.config.AuthAutoConfiguration",
        "com.klzw.common.redis.config.RedisAutoConfiguration",
        "com.klzw.common.redis.config.RedissonAutoConfiguration",
        "org.redisson.spring.starter.RedissonAutoConfigurationV4",
        "org.redisson.spring.starter.RedissonAutoConfigurationV2",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        // MongoDB 相关自动配置
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
    }
)
@MapperScan("com.klzw.common.database.mapper")
public class TestApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(TestApplication.class, args);
    }
}
