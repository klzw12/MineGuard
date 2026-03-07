package com.klzw.common.redis.config;

import com.klzw.common.redis.constant.RedisConstants;
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
     * Redis主机地址
     */
    private String host = "localhost";

    /**
     * Redis端口
     */
    private int port = 6379;

    /**
     * Redis密码
     */
    private String password = "";

    /**
     * Redis数据库索引
     */
    private int database = 0;

    /**
     * 连接超时时间（毫秒）
     */
    private long timeout = 60000;

    /**
     * 缓存键前缀
     */
    private String keyPrefix = RedisConstants.PREFIX;

    /**
     * 默认过期时间（秒）
     */
    private int defaultExpire = 3600;

    /**
     * 连接池配置
     */
    private PoolConfig pool = new PoolConfig();

    /**
     * 分布式锁配置
     */
    private LockConfig lock = new LockConfig();

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 连接池配置类
     */
    @Data
    public static class PoolConfig {
        /**
         * 最大活跃连接数
         */
        private int maxActive = 8;

        /**
         * 最大等待时间（毫秒）
         */
        private long maxWait = -1;

        /**
         * 最大空闲连接数
         */
        private int maxIdle = 8;

        /**
         * 最小空闲连接数
         */
        private int minIdle = 0;
    }

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
