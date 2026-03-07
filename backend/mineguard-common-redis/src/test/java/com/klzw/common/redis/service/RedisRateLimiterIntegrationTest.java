package com.klzw.common.redis.service;

import com.klzw.common.redis.AbstractRedisIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 限流器集成测试
 */
@DisplayName("Redis限流器集成测试")
@Tag("integration")
public class RedisRateLimiterIntegrationTest extends AbstractRedisIntegrationTest {

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    private String testKey;

    @BeforeEach
    void setUp() {
        testKey = "test:rate:limit:" + UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() {
        if (testKey != null) {
            redisRateLimiter.reset(testKey);
        }
    }

    @Test
    @DisplayName("滑动窗口限流-正常请求")
    void testTryAcquireSlidingWindow() {
        int limit = 5;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired, "第" + (i + 1) + "次请求应该成功");
        }
        
        Long count = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(limit, count);
    }

    @Test
    @DisplayName("滑动窗口限流-超过限制")
    void testTryAcquireSlidingWindowExceed() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired, "第" + (i + 1) + "次请求应该成功");
        }
        
        boolean acquired = redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.MINUTES);
        assertFalse(acquired, "超过限制后请求应该失败");
    }

    @Test
    @DisplayName("滑动窗口限流-窗口重置")
    void testTryAcquireSlidingWindowReset() throws InterruptedException {
        int limit = 2;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.SECONDS);
        }
        
        boolean acquired = redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.SECONDS);
        assertFalse(acquired, "超过限制后请求应该失败");
        
        Thread.sleep(1100);
        
        acquired = redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.SECONDS);
        assertTrue(acquired, "窗口重置后请求应该成功");
    }

    @Test
    @DisplayName("令牌桶限流-正常请求")
    void testTryAcquireTokenBucket() {
        int limit = 5;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired, "第" + (i + 1) + "次请求应该成功");
        }
    }

    @Test
    @DisplayName("令牌桶限流-超过限制")
    void testTryAcquireTokenBucketExceed() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired, "第" + (i + 1) + "次请求应该成功");
        }
        
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.MINUTES);
        assertFalse(acquired, "超过限制后请求应该失败");
    }

    @Test
    @DisplayName("令牌桶限流-令牌恢复")
    void testTryAcquireTokenBucketRecover() throws InterruptedException {
        int limit = 2;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.SECONDS);
        }
        
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.SECONDS);
        assertFalse(acquired, "超过限制后请求应该失败");
        
        Thread.sleep(600);
        
        double tokens = redisRateLimiter.getCurrentTokens(testKey);
        assertTrue(tokens >= 0, "令牌数应该大于等于0");
    }

    @Test
    @DisplayName("重置限流计数")
    void testReset() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.MINUTES);
        }
        
        Long count = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(limit, count, "重置前计数应该等于limit");
        
        redisRateLimiter.reset(testKey);
        
        count = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(0L, count, "重置后计数应该为0");
    }

    @Test
    @DisplayName("获取当前计数")
    void testGetCurrentCount() {
        int limit = 5;
        long window = 1;
        
        Long count = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(0L, count, "初始计数应该为0");
        
        for (int i = 0; i < 3; i++) {
            redisRateLimiter.tryAcquire(testKey, limit, window, TimeUnit.MINUTES);
        }
        
        count = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(3L, count, "请求3次后计数应该为3");
    }

    @Test
    @DisplayName("获取当前令牌数")
    void testGetCurrentTokens() {
        int limit = 5;
        long window = 1;
        
        double tokens = redisRateLimiter.getCurrentTokens(testKey);
        assertEquals(0.0, tokens, 0.001, "初始令牌数应该为0");
        
        redisRateLimiter.tryAcquireWithTokenBucket(testKey, limit, window, TimeUnit.MINUTES);
        
        tokens = redisRateLimiter.getCurrentTokens(testKey);
        assertTrue(tokens >= 0 && tokens < limit, "请求后令牌数应该在有效范围内");
    }
}
