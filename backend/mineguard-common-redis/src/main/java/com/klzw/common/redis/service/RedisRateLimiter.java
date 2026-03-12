package com.klzw.common.redis.service;

import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import com.klzw.common.redis.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire(String key, int limit, long window, TimeUnit timeUnit) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        long currentTime = System.currentTimeMillis();
        long windowMillis = timeUnit.toMillis(window);
        long windowStart = currentTime - windowMillis;

        redisTemplate.opsForZSet().add(rateLimitKey, currentTime, currentTime);
        redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);
        redisTemplate.expire(rateLimitKey, window, timeUnit);

        long count = redisTemplate.opsForZSet().size(rateLimitKey);

        return count <= limit;
    }

    public void tryAcquireOrThrow(String key, int limit, long window, TimeUnit timeUnit) {
        if (!tryAcquire(key, limit, window, timeUnit)) {
            throw new RedisException(RedisResultCode.RATE_LIMIT_EXCEEDED, "超过限流阈值: " + key);
        }
    }

    public boolean tryAcquireWithTokenBucket(String key, int limit, long window, TimeUnit timeUnit) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey("token:" + key);
        long currentTime = System.currentTimeMillis();
        long windowMillis = timeUnit.toMillis(window);
        double tokensPerMillisecond = (double) limit / windowMillis;

        Object value = redisTemplate.opsForValue().get(rateLimitKey);
        long lastUpdateTime = currentTime;
        double currentTokens = limit;

        if (value != null) {
            String[] parts = value.toString().split(",");
            currentTokens = Double.parseDouble(parts[0]);
            lastUpdateTime = Long.parseLong(parts[1]);
        }

        long elapsedTime = currentTime - lastUpdateTime;
        double newTokens = Math.min(limit, currentTokens + (elapsedTime * tokensPerMillisecond));

        if (newTokens >= 1) {
            newTokens -= 1;
            redisTemplate.opsForValue().set(rateLimitKey, newTokens + "," + currentTime, window, timeUnit);
            return true;
        } else {
            return false;
        }
    }

    public void tryAcquireWithTokenBucketOrThrow(String key, int limit, long window, TimeUnit timeUnit) {
        if (!tryAcquireWithTokenBucket(key, limit, window, timeUnit)) {
            throw new RedisException(RedisResultCode.RATE_LIMIT_EXCEEDED, "超过限流阈值: " + key);
        }
    }

    public Long getCurrentCount(String key) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        return redisTemplate.opsForZSet().size(rateLimitKey);
    }

    public void reset(String key) {
        String rateLimitKey = RedisKeyUtil.generateRateLimitKey(key);
        String tokenBucketKey = RedisKeyUtil.generateRateLimitKey("token:" + key);
        redisTemplate.delete(rateLimitKey);
        redisTemplate.delete(tokenBucketKey);
    }

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
