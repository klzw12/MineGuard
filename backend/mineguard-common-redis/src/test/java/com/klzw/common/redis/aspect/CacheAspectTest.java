package com.klzw.common.redis.aspect;

import com.klzw.common.redis.annotation.Cacheable;
import com.klzw.common.redis.annotation.CacheEvict;
import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheAspect 测试类
 */
@SpringBootTest
public class CacheAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private RedisCacheService redisCacheService;

    private static final String TEST_KEY_PREFIX = "test:cache";
    private static final String TEST_KEY = TEST_KEY_PREFIX + ":test";
    private static final String TEST_VALUE = "test:value";

    @Test
    public void testCacheable() {
        // 清理缓存
        redisCacheService.delete(TEST_KEY);
        
        // 第一次调用，缓存未命中，应该执行方法
        String result1 = testService.getCachedValue("test");
        assertEquals(TEST_VALUE, result1);
        
        // 第二次调用，缓存命中，应该直接返回缓存值
        String result2 = testService.getCachedValue("test");
        assertEquals(TEST_VALUE, result2);
        
        // 验证缓存是否存在
        assertTrue(redisCacheService.exists(TEST_KEY));
    }

    @Test
    public void testCacheEvict() {
        // 先设置缓存
        redisCacheService.set(TEST_KEY, TEST_VALUE);
        assertTrue(redisCacheService.exists(TEST_KEY));
        
        // 调用带有 @CacheEvict 注解的方法
        String result = testService.evictCachedValue("test");
        assertEquals("evicted", result);
        
        // 验证缓存是否被清除
        assertFalse(redisCacheService.exists(TEST_KEY));
    }

    @Test
    public void testCacheableWithSpEL() {
        // 清理缓存
        String dynamicKey = TEST_KEY_PREFIX + ":123";
        redisCacheService.delete(dynamicKey);
        
        // 调用带有 SpEL 表达式的方法
        String result1 = testService.getCachedValueWithSpEL(123);
        assertEquals(TEST_VALUE, result1);
        
        // 验证缓存是否存在
        assertTrue(redisCacheService.exists(dynamicKey));
        
        // 第二次调用，缓存命中
        String result2 = testService.getCachedValueWithSpEL(123);
        assertEquals(TEST_VALUE, result2);
    }

    @Test
    public void testCacheEvictWithSpEL() {
        // 先设置缓存
        String dynamicKey = TEST_KEY_PREFIX + ":456";
        redisCacheService.set(dynamicKey, TEST_VALUE);
        assertTrue(redisCacheService.exists(dynamicKey));
        
        // 调用带有 SpEL 表达式的方法
        String result = testService.evictCachedValueWithSpEL(456);
        assertEquals("evicted", result);
        
        // 验证缓存是否被清除
        assertFalse(redisCacheService.exists(dynamicKey));
    }

    @Test
    public void testCacheExpiration() throws InterruptedException {
        // 清理缓存
        redisCacheService.delete(TEST_KEY);
        
        // 调用带有过期时间的缓存方法
        String result1 = testService.getCachedValueWithExpiration("test");
        assertEquals(TEST_VALUE, result1);
        
        // 验证缓存是否存在
        assertTrue(redisCacheService.exists(TEST_KEY));
        
        // 等待缓存过期
        Thread.sleep(1500);
        
        // 验证缓存是否过期
        assertFalse(redisCacheService.exists(TEST_KEY));
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 清理缓存
        redisCacheService.delete(TEST_KEY);
        
        // 测试缓存设置和获取
        String result1 = testService.getCachedValue("test");
        assertEquals(TEST_VALUE, result1);
        
        // 验证缓存是否存在
        assertTrue(redisCacheService.exists(TEST_KEY));
        
        // 测试缓存清除
        String result2 = testService.evictCachedValue("test");
        assertEquals("evicted", result2);
        
        // 验证缓存是否被清除
        assertFalse(redisCacheService.exists(TEST_KEY));
    }

    /**
     * 测试服务类，用于测试缓存注解
     */
    @org.springframework.stereotype.Service
    public static class TestService {

        @Cacheable(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#param", expire = 1, timeUnit = TimeUnit.SECONDS)
        public String getCachedValue(String param) {
            // 模拟业务逻辑
            return TEST_VALUE;
        }

        @Cacheable(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#id", expire = 1, timeUnit = TimeUnit.SECONDS)
        public String getCachedValueWithSpEL(int id) {
            // 模拟业务逻辑
            return TEST_VALUE;
        }

        @Cacheable(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#param", expire = 1, timeUnit = TimeUnit.SECONDS)
        public String getCachedValueWithExpiration(String param) {
            // 模拟业务逻辑
            return TEST_VALUE;
        }

        @CacheEvict(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#param")
        public String evictCachedValue(String param) {
            // 模拟业务逻辑
            return "evicted";
        }

        @CacheEvict(keyPrefix = TEST_KEY_PREFIX, keySuffix = "#id")
        public String evictCachedValueWithSpEL(int id) {
            // 模拟业务逻辑
            return "evicted";
        }
    }
}
