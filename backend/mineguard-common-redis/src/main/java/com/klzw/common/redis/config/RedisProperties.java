package com.klzw.common.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 自定义配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mineguard.redis")
public class RedisProperties {

    /**
     * 缓存键前缀
     */
    private String keyPrefix = "mineguard:";

    /**
     * 默认过期时间（秒）
     */
    private int defaultExpire = 3600;

    /**
     * 分布式锁配置
     */
    private LockConfig lock = new LockConfig();

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 分布式锁配置类
     */
    @Data
    public static class LockConfig {
        /**
         * 默认过期时间（秒）
         */
        private int defaultExpire = 30;

        /**
         * 重试次数
         */
        private int retryCount = 3;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 100;
    }

    /**
     * 限流配置类
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 默认限流窗口（秒）
         */
        private int defaultWindow = 60;

        /**
         * 默认限流次数
         */
        private int defaultLimit = 100;
    }
}