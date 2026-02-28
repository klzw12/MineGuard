package com.klzw.common.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存统一配置类
 */
@Configuration
public class CacheConfig {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 获取缓存键前缀
     * @return 缓存键前缀
     */
    public String getKeyPrefix() {
        return redisProperties.getKeyPrefix();
    }

    /**
     * 获取默认过期时间
     * @return 默认过期时间（秒）
     */
    public int getDefaultExpire() {
        return redisProperties.getDefaultExpire();
    }

    /**
     * 获取分布式锁默认过期时间
     * @return 分布式锁默认过期时间（秒）
     */
    public int getLockDefaultExpire() {
        return redisProperties.getLock().getDefaultExpire();
    }

    /**
     * 获取分布式锁重试次数
     * @return 分布式锁重试次数
     */
    public int getLockRetryCount() {
        return redisProperties.getLock().getRetryCount();
    }

    /**
     * 获取分布式锁重试间隔
     * @return 分布式锁重试间隔（毫秒）
     */
    public int getLockRetryInterval() {
        return redisProperties.getLock().getRetryInterval();
    }

    /**
     * 获取限流默认窗口
     * @return 限流默认窗口（秒）
     */
    public int getRateLimitDefaultWindow() {
        return redisProperties.getRateLimit().getDefaultWindow();
    }

    /**
     * 获取限流默认次数
     * @return 限流默认次数
     */
    public int getRateLimitDefaultLimit() {
        return redisProperties.getRateLimit().getDefaultLimit();
    }
}