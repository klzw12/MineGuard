package com.klzw.common.redis.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisRateLimiter 测试类
 */
@SpringBootTest
public class RedisRateLimiterTest {

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    private static final String TEST_RATE_LIMIT_KEY = "test:rate:limit";
    private static final int TEST_LIMIT = 5;
    private static final long TEST_WINDOW = 1;
    private static final TimeUnit TEST_TIME_UNIT = TimeUnit.MINUTES;

    @Test
    public void testTryAcquireWithSlidingWindow() {
        // 测试滑动窗口限流
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        // 测试在限制范围内的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
            assertTrue(acquired);
        }
        
        // 测试超出限制的请求
        boolean acquired = redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
        assertFalse(acquired);
        
        // 验证当前计数
        Long currentCount = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(TEST_LIMIT + 1, currentCount);
    }

    @Test
    public void testTryAcquireWithTokenBucket() {
        // 测试令牌桶限流
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        // 测试在限制范围内的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
            assertTrue(acquired);
        }
        
        // 测试超出限制的请求
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
        assertFalse(acquired);
        
        // 验证当前令牌数
        double currentTokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertEquals(0.0, currentTokens, 0.001);
    }

    @Test
    public void testGetCurrentCount() {
        // 测试获取当前计数
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        // 发送几个请求
        for (int i = 0; i < 3; i++) {
            redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
        }
        
        Long currentCount = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(3L, currentCount);
    }

    @Test
    public void testReset() {
        // 测试重置限流计数
        // 发送几个请求
        for (int i = 0; i < 3; i++) {
            redisRateLimiter.tryAcquire(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
        }
        
        // 重置
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        // 验证计数已重置
        Long currentCount = redisRateLimiter.getCurrentCount(TEST_RATE_LIMIT_KEY);
        assertEquals(0L, currentCount);
        
        // 验证令牌桶也已重置
        double currentTokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertEquals(0.0, currentTokens, 0.001);
    }

    @Test
    public void testGetCurrentTokens() {
        // 测试获取当前令牌数
        redisRateLimiter.reset(TEST_RATE_LIMIT_KEY);
        
        // 发送几个请求
        for (int i = 0; i < 2; i++) {
            redisRateLimiter.tryAcquireWithTokenBucket(TEST_RATE_LIMIT_KEY, TEST_LIMIT, TEST_WINDOW, TEST_TIME_UNIT);
        }
        
        double currentTokens = redisRateLimiter.getCurrentTokens(TEST_RATE_LIMIT_KEY);
        assertEquals(TEST_LIMIT - 2, currentTokens, 0.001);
    }

    @Test
    public void testSlidingWindowTimeEffect() throws InterruptedException {
        // 测试滑动窗口的时间窗口效果
        String testKey = "test:sliding:window";
        redisRateLimiter.reset(testKey);
        
        // 发送几个请求
        for (int i = 0; i < 3; i++) {
            redisRateLimiter.tryAcquire(testKey, 5, 1, TimeUnit.SECONDS);
        }
        
        // 验证当前计数
        Long currentCount = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(3L, currentCount);
        
        // 等待时间窗口过期
        Thread.sleep(1500);
        
        // 再次发送请求
        boolean acquired = redisRateLimiter.tryAcquire(testKey, 5, 1, TimeUnit.SECONDS);
        assertTrue(acquired);
        
        // 验证当前计数（应该只有1个新请求）
        currentCount = redisRateLimiter.getCurrentCount(testKey);
        assertEquals(1L, currentCount);
    }

    @Test
    public void testTokenBucketTokenRecovery() throws InterruptedException {
        // 测试令牌桶的令牌恢复效果
        String testKey = "test:token:recovery";
        redisRateLimiter.reset(testKey);
        
        // 消耗所有令牌
        for (int i = 0; i < TEST_LIMIT; i++) {
            redisRateLimiter.tryAcquireWithTokenBucket(testKey, TEST_LIMIT, 1, TimeUnit.SECONDS);
        }
        
        // 验证令牌已耗尽
        boolean acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, TEST_LIMIT, 1, TimeUnit.SECONDS);
        assertFalse(acquired);
        
        // 等待令牌恢复（等待一半时间）
        Thread.sleep(500);
        
        // 验证是否有令牌恢复
        double currentTokens = redisRateLimiter.getCurrentTokens(testKey);
        assertTrue(currentTokens > 0 && currentTokens < TEST_LIMIT);
        
        // 尝试获取令牌
        acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, TEST_LIMIT, 1, TimeUnit.SECONDS);
        assertTrue(acquired);
    }

    @Test
    public void testDifferentTimeUnits() {
        // 测试不同时间单位的限流
        String testKey = "test:different:timeunits";
        redisRateLimiter.reset(testKey);
        
        // 测试秒级限流
        for (int i = 0; i < 3; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(testKey, 3, 1, TimeUnit.SECONDS);
            assertTrue(acquired);
        }
        
        // 测试超出限制
        boolean acquired = redisRateLimiter.tryAcquire(testKey, 3, 1, TimeUnit.SECONDS);
        assertFalse(acquired);
    }

    @Test
    public void testZeroLimit() {
        // 测试限制为0的情况
        String testKey = "test:zero:limit";
        redisRateLimiter.reset(testKey);
        
        // 验证请求被拒绝
        boolean acquired = redisRateLimiter.tryAcquire(testKey, 0, 1, TimeUnit.MINUTES);
        assertFalse(acquired);
        
        // 验证令牌桶也被拒绝
        acquired = redisRateLimiter.tryAcquireWithTokenBucket(testKey, 0, 1, TimeUnit.MINUTES);
        assertFalse(acquired);
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 测试与真实Redis的集成
        String integrationKey = "integration:rate:limit";
        redisRateLimiter.reset(integrationKey);
        
        // 测试滑动窗口限流
        for (int i = 0; i < 3; i++) {
            boolean acquired = redisRateLimiter.tryAcquire(integrationKey, 3, 1, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
        
        // 测试超出限制
        boolean acquired = redisRateLimiter.tryAcquire(integrationKey, 3, 1, TimeUnit.MINUTES);
        assertFalse(acquired);
        
        // 测试令牌桶限流
        redisRateLimiter.reset(integrationKey);
        for (int i = 0; i < 3; i++) {
            acquired = redisRateLimiter.tryAcquireWithTokenBucket(integrationKey, 3, 1, TimeUnit.MINUTES);
            assertTrue(acquired);
        }
        
        // 测试超出限制
        acquired = redisRateLimiter.tryAcquireWithTokenBucket(integrationKey, 3, 1, TimeUnit.MINUTES);
        assertFalse(acquired);
    }
}
