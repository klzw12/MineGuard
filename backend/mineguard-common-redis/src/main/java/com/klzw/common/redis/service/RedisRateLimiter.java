package com.klzw.common.redis.service;

import com.klzw.common.redis.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 限流器服务
 */
@Service
public class RedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRateLimiter(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 限流检查（基于滑动窗口算法）
     * @param key 限流键
     * @param limit 限制次数
     * @param window 时间窗口
     * @param timeUnit 时间单位
     * @return 是否通过限流
     */
    public boolean tryAcquire(String key, int limit, long window, TimeUnit timeUnit) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        long currentTime = System.currentTimeMillis();
        long windowMillis = timeUnit.toMillis(window);
        long windowStart = currentTime - windowMillis;

        // 使用Redis的ZADD命令添加当前时间戳
        redisTemplate.opsForZSet().add(rateLimitKey, currentTime, currentTime);

        // 移除窗口外的记录
        redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);

        // 设置过期时间，避免内存泄漏
        redisTemplate.expire(rateLimitKey, window, timeUnit);

        // 获取窗口内的记录数
        long count = redisTemplate.opsForZSet().size(rateLimitKey);

        // 检查是否超过限制
        return count <= limit;
    }

    /**
     * 限流检查（基于令牌桶算法）
     * @param key 限流键
     * @param limit 限制次数
     * @param window 时间窗口
     * @param timeUnit 时间单位
     * @return 是否通过限流
     */
    public boolean tryAcquireWithTokenBucket(String key, int limit, long window, TimeUnit timeUnit) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey("token:" + key);
        long currentTime = System.currentTimeMillis();
        long windowMillis = timeUnit.toMillis(window);
        double tokensPerMillisecond = (double) limit / windowMillis;

        // 获取当前令牌数和上次更新时间
        Object value = redisTemplate.opsForValue().get(rateLimitKey);
        long lastUpdateTime = currentTime;
        double currentTokens = limit;

        if (value != null) {
            String[] parts = value.toString().split(",");
            currentTokens = Double.parseDouble(parts[0]);
            lastUpdateTime = Long.parseLong(parts[1]);
        }

        // 计算新的令牌数
        long elapsedTime = currentTime - lastUpdateTime;
        double newTokens = Math.min(limit, currentTokens + (elapsedTime * tokensPerMillisecond));

        if (newTokens >= 1) {
            // 消耗一个令牌
            newTokens -= 1;
            // 更新令牌数和时间
            redisTemplate.opsForValue().set(rateLimitKey, newTokens + "," + currentTime, window, timeUnit);
            return true;
        } else {
            // 令牌不足
            return false;
        }
    }

    /**
     * 获取当前计数
     * @param key 限流键
     * @return 当前计数
     */
    public Long getCurrentCount(String key) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        return redisTemplate.opsForZSet().size(rateLimitKey);
    }

    /**
     * 重置限流计数
     * @param key 限流键
     */
    public void reset(String key) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        String tokenBucketKey = RedisKeyUtil.generateRateLimitKey("token:" + key);
        redisTemplate.delete(rateLimitKey);
        redisTemplate.delete(tokenBucketKey);
    }

    /**
     * 获取令牌桶当前令牌数
     * @param key 限流键
     * @return 当前令牌数
     */
    public double getCurrentTokens(String key) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey("token:" + key);
        Object value = redisTemplate.opsForValue().get(rateLimitKey);
        if (value != null) {
            String[] parts = value.toString().split(",");
            return Double.parseDouble(parts[0]);
        }
        return 0;
    }
}

