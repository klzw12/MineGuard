package com.klzw.common.redis.service;

import com.klzw.common.redis.AbstractRedisIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private static final String TEST_RATE_LIMIT_KEY = "test:rate:limit";

    @BeforeEach
    void setUp() {
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
    }

    @AfterEach
    void tearDown() {
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
    }

    @Test
    @DisplayName("滑动窗口限流-正常请求")
    void testTryAcquireSlidingWindow() {
        int limit = 5;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
        
        Long count = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(limit, count);
    }

    @Test
    @DisplayName("滑动窗口限流-超过限制")
    void testTryAcquireSlidingWindowExceed() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
        
        boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
        assertFalse(acquired);
    }

    @Test
    @DisplayName("滑动窗口限流-窗口重置")
    void testTryAcquireSlidingWindowReset() throws InterruptedException {
        int limit = 2;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.SECONDS);
        }
        
        boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.SECONDS);
        assertFalse(acquired);
        
        Thread.sleep(1100);
        
        acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.SECONDS);
        assertTrue(acquired);
    }

    @Test
    @DisplayName("令牌桶限流-正常请求")
    void testTryAcquireTokenBucket() {
        int limit = 5;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
    }

    @Test
    @DisplayName("令牌桶限流-超过限制")
    void testTryAcquireTokenBucketExceed() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
        
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
        assertFalse(acquired);
    }

    @Test
    @DisplayName("令牌桶限流-令牌恢复")
    void testTryAcquireTokenBucketRecover() throws InterruptedException {
        int limit = 2;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.SECONDS);
        }
        
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.SECONDS);
        assertFalse(acquired);
        
        Thread.sleep(600);
        
        double tokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertTrue(tokens >= 0);
    }

    @Test
    @DisplayName("重置限流计数")
    void testReset() {
        int limit = 3;
        long window = 1;
        
        for (int i = 0; i < limit; i++) {
            redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
        }
        
        Long count = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(limit, count);
        
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        count = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(0L, count);
    }

    @Test
    @DisplayName("获取当前计数")
    void testGetCurrentCount() {
        int limit = 5;
        long window = 1;
        
        Long count = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(0L, count);
        
        for (int i = 0; i < 3; i++) {
            redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
        }
        
        count = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(3L, count);
    }

    @Test
    @DisplayName("获取当前令牌数")
    void testGetCurrentTokens() {
        int limit = 5;
        long window = 1;
        
        double tokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertEquals(0.0, tokens, 0.001);
        
        redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, limit, window, TimeUnit.MINUTES);
        
        tokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertTrue(tokens >= 0 && tokens < limit);
    }
}
