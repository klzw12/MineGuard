package com.klzw.common.redis.aspect;

import com.klzw.common.redis.annotation.RateLimit;
import com.klzw.common.redis.exception.RedisException;
import com.klzw.common.redis.service.RedisRateLimiter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimitAspect 测试类
 */
@SpringBootTest
public class RateLimitAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    private static final String TEST_KEY_PREFIX = "test:rate:limit";
    private static final String TEST_KEY = TEST_KEY_PREFIX + ":test";
    private static final int TEST_LIMIT = 3;

    @Test
    public void testRateLimitWithinLimit() {
        // 清理限流计数
        redisRateLimiter.reset(TEST_KEY);
        
        // 在限制范围内的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            String result = testService.rateLimitedMethod("test");
            assertEquals("success", result);
        }
    }

    @Test
    public void testRateLimitExceeded() {
        // 清理限流计数
        redisRateLimiter.reset(TEST_KEY);
        
        // 发送超出限制的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            testService.rateLimitedMethod("test");
        }
        
        // 超出限制的请求应该抛出异常
        assertThrows(RedisException.class, () -> {
            testService.rateLimitedMethod("test");
        });
    }

    @Test
    public void testRateLimitWithSpEL() {
        // 清理限流计数
        String dynamicKey = TEST_KEY_PREFIX + ":123";
        redisRateLimiter.reset(dynamicKey);
        
        // 在限制范围内的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            String result = testService.rateLimitedMethodWithSpEL(123);
            assertEquals("success", result);
        }
        
        // 超出限制的请求应该抛出异常
        assertThrows(RedisException.class, () -> {
            testService.rateLimitedMethodWithSpEL(123);
        });
    }

    @Test
    public void testRateLimitDifferentKeys() {
        // 清理限流计数
        redisRateLimiter.reset(TEST_KEY_PREFIX + ":key1");
        redisRateLimiter.reset(TEST_KEY_PREFIX + ":key2");
        
        // 测试第一个键
        for (int i = 0; i < TEST_LIMIT; i++) {
            String result = testService.rateLimitedMethod("key1");
            assertEquals("success", result);
        }
        
        // 测试第二个键，应该不受第一个键的影响
        for (int i = 0; i < TEST_LIMIT; i++) {
            String result = testService.rateLimitedMethod("key2");
            assertEquals("success", result);
        }
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 清理限流计数
        redisRateLimiter.reset(TEST_KEY);
        
        // 测试在限制范围内的请求
        for (int i = 0; i < TEST_LIMIT; i++) {
            String result = testService.rateLimitedMethod("test");
            assertEquals("success", result);
        }
        
        // 测试超出限制的请求
        assertThrows(RedisException.class, () -> {
            testService.rateLimitedMethod("test");
        });
    }

    /**
     * 测试服务类，用于测试限流注解
     */
    @org.springframework.stereotype.Service
    public static class TestService {

        @RateLimit(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#param", limit = TEST_LIMIT, window = 1, timeUnit = TimeUnit.MINUTES, message = "Rate limit exceeded")
        public String rateLimitedMethod(String param) {
            // 模拟业务逻辑
            return "success";
        }

        @RateLimit(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#id", limit = TEST_LIMIT, window = 1, timeUnit = TimeUnit.MINUTES, message = "Rate limit exceeded")
        public String rateLimitedMethodWithSpEL(int id) {
            // 模拟业务逻辑
            return "success";
        }
    }
}
