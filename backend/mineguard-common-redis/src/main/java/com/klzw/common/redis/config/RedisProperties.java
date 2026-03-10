package com.klzw.common.redis.config;

import com.klzw.common.redis.constant.RedisConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.redis")
public class RedisProperties {

    private boolean enabled = true;

    private String keyPrefix = RedisConstants.PREFIX;

    private int defaultExpire = 3600;

    private LockConfig lock = new LockConfig();

    private RateLimitConfig rateLimit = new RateLimitConfig();

    private RedissonConfig redisson = new RedissonConfig();

    @Data
    public static class LockConfig {
        private int defaultExpire = 30;
        private int retryCount = 3;
        private int retryInterval = 100;
    }

    @Data
    public static class RateLimitConfig {
        private int defaultWindow = 60;
        private int defaultLimit = 100;
    }

    @Data
    public static class RedissonConfig {
        private boolean enabled = true;
        private int retryAttempts = 3;
        private int retryInterval = 1000;
        private int connectTimeout = 30000;
    }
}
