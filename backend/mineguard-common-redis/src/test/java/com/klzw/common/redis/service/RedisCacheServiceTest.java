package com.klzw.common.redis.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisCacheService 测试类
 */
@SpringBootTest
public class RedisCacheServiceTest {

    @Autowired
    private RedisCacheService redisCacheService;

    private static final String TEST_KEY = "test:key";
    private static final String TEST_VALUE = "test:value";
    private static final long TEST_EXPIRE = 1;
    private static final TimeUnit TEST_TIME_UNIT = TimeUnit.MINUTES;

    @Test
    public void testSetAndGet() {
        // 测试设置和获取
        redisCacheService.set(TEST_KEY, TEST_VALUE, TEST_EXPIRE, TEST_TIME_UNIT);
        String value = redisCacheService.get(TEST_KEY);
        assertEquals(TEST_VALUE, value);
    }

    @Test
    public void testDelete() {
        // 测试删除
        redisCacheService.set(TEST_KEY, TEST_VALUE);
        assertTrue(redisCacheService.exists(TEST_KEY));
        redisCacheService.delete(TEST_KEY);
        assertFalse(redisCacheService.exists(TEST_KEY));
    }

    @Test
    public void testExists() {
        // 测试存在性检查
        redisCacheService.set(TEST_KEY, TEST_VALUE);
        assertTrue(redisCacheService.exists(TEST_KEY));
        redisCacheService.delete(TEST_KEY);
        assertFalse(redisCacheService.exists(TEST_KEY));
    }

    @Test
    public void testExpire() {
        // 测试过期时间设置
        redisCacheService.set(TEST_KEY, TEST_VALUE);
        assertTrue(redisCacheService.expire(TEST_KEY, TEST_EXPIRE, TEST_TIME_UNIT));
        Long expire = redisCacheService.getExpire(TEST_KEY, TEST_TIME_UNIT);
        assertNotNull(expire);
        assertTrue(expire > 0);
    }

    @Test
    public void testIncrementAndDecrement() {
        // 测试自增和自减
        String counterKey = "test:counter";
        redisCacheService.delete(counterKey);
        
        Long incrementResult = redisCacheService.increment(counterKey);
        assertEquals(1L, incrementResult);
        
        incrementResult = redisCacheService.increment(counterKey, 5);
        assertEquals(6L, incrementResult);
        
        Long decrementResult = redisCacheService.decrement(counterKey);
        assertEquals(5L, decrementResult);
        
        decrementResult = redisCacheService.decrement(counterKey, 2);
        assertEquals(3L, decrementResult);
    }

    @Test
    public void testSetBatch() {
        // 测试批量设置
        Map<String, Object> map = new HashMap<>();
        map.put("test:batch:1", "value1");
        map.put("test:batch:2", "value2");
        map.put("test:batch:3", "value3");
        
        redisCacheService.setBatch(map, TEST_EXPIRE, TEST_TIME_UNIT);
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String value = redisCacheService.get(entry.getKey());
            assertEquals(entry.getValue(), value);
        }
    }

    @Test
    public void testGetBatch() {
        // 测试批量获取
        Map<String, Object> map = new HashMap<>();
        map.put("test:batch:1", "value1");
        map.put("test:batch:2", "value2");
        map.put("test:batch:3", "value3");
        
        redisCacheService.setBatch(map);
        
        Map<String, String> result = redisCacheService.getBatch(map.keySet());
        assertEquals(map.size(), result.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            assertEquals(entry.getValue(), result.get(entry.getKey()));
        }
    }

    @Test
    public void testDeleteBatch() {
        // 测试批量删除
        Map<String, Object> map = new HashMap<>();
        map.put("test:batch:1", "value1");
        map.put("test:batch:2", "value2");
        map.put("test:batch:3", "value3");
        
        redisCacheService.setBatch(map);
        
        Long deleted = redisCacheService.deleteBatch(map.keySet());
        assertEquals(map.size(), deleted);
        
        for (String key : map.keySet()) {
            assertFalse(redisCacheService.exists(key));
        }
    }

    @Test
    public void testExistsBatch() {
        // 测试批量存在性检查
        Map<String, Object> map = new HashMap<>();
        map.put("test:batch:1", "value1");
        map.put("test:batch:2", "value2");
        
        redisCacheService.setBatch(map);
        
        Map<String, Boolean> result = redisCacheService.existsBatch(map.keySet());
        assertEquals(map.size(), result.size());
        for (String key : map.keySet()) {
            assertTrue(result.get(key));
        }
    }

    @Test
    public void testExpireBatch() {
        // 测试批量设置过期时间
        Map<String, Object> map = new HashMap<>();
        map.put("test:batch:1", "value1");
        map.put("test:batch:2", "value2");
        
        redisCacheService.setBatch(map);
        
        int successCount = redisCacheService.expireBatch(map.keySet(), TEST_EXPIRE, TEST_TIME_UNIT);
        assertEquals(map.size(), successCount);
    }

    @Test
    public void testHashOperations() {
        // 测试哈希操作
        String hashKey = "test:hash";
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        String value2 = "value2";
        
        // 测试哈希设置
        redisCacheService.hSet(hashKey, field1, value1);
        String result1 = redisCacheService.hGet(hashKey, field1);
        assertEquals(value1, result1);
        
        // 测试哈希批量设置
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(field1, value1);
        hashMap.put(field2, value2);
        redisCacheService.hSetBatch(hashKey, hashMap);
        
        // 测试哈希批量获取
        Map<String, String> hashResult = redisCacheService.hGetBatch(hashKey, hashMap.keySet());
        assertEquals(hashMap.size(), hashResult.size());
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            assertEquals(entry.getValue(), hashResult.get(entry.getKey()));
        }
        
        // 测试哈希删除
        Long deleted = redisCacheService.hDelete(hashKey, field1, field2);
        assertEquals(2L, deleted);
    }

    @Test
    public void testGetNonExistentKey() {
        // 测试获取不存在的键
        String nonExistentKey = "test:non:existent";
        redisCacheService.delete(nonExistentKey);
        
        Object value = redisCacheService.get(nonExistentKey);
        assertNull(value);
    }

    @Test
    public void testBatchOperationsWithEmptyCollections() {
        // 测试批量操作时传入空集合
        Map<String, Object> emptyMap = new HashMap<>();
        redisCacheService.setBatch(emptyMap);
        
        Map<String, String> emptyGetResult = redisCacheService.getBatch(emptyMap.keySet());
        assertTrue(emptyGetResult.isEmpty());
        
        Long emptyDeleteResult = redisCacheService.deleteBatch(emptyMap.keySet());
        assertEquals(0L, emptyDeleteResult);
        
        Map<String, Boolean> emptyExistsResult = redisCacheService.existsBatch(emptyMap.keySet());
        assertTrue(emptyExistsResult.isEmpty());
        
        int emptyExpireResult = redisCacheService.expireBatch(emptyMap.keySet(), TEST_EXPIRE, TEST_TIME_UNIT);
        assertEquals(0, emptyExpireResult);
        
        // 测试哈希批量操作时传入空Map
        String hashKey = "test:hash:empty";
        redisCacheService.hSetBatch(hashKey, emptyMap);
        
        Map<String, String> emptyHashGetResult = redisCacheService.hGetBatch(hashKey, emptyMap.keySet());
        assertTrue(emptyHashGetResult.isEmpty());
    }

    @Test
    public void testExpireWithZeroTime() {
        // 测试过期时间为0的情况
        String key = "test:expire:zero";
        redisCacheService.set(key, TEST_VALUE);
        
        boolean result = redisCacheService.expire(key, 0, TEST_TIME_UNIT);
        assertTrue(result);
        
        // 验证键已被删除
        assertFalse(redisCacheService.exists(key));
    }

    @Test
    public void testIncrementDecrementWithNonExistentKey() {
        // 测试自增自减操作在键不存在时的行为
        String counterKey = "test:counter:new";
        redisCacheService.delete(counterKey);
        
        // 自增不存在的键，应该返回1
        Long incrementResult = redisCacheService.increment(counterKey);
        assertEquals(1L, incrementResult);
        
        // 自减，应该返回0
        Long decrementResult = redisCacheService.decrement(counterKey);
        assertEquals(0L, decrementResult);
    }

    @Test
    public void testSetWithNullValue() {
        // 测试设置缓存时传入null值
        String key = "test:null:value";
        redisCacheService.set(key, null);
        
        // 验证键存在
        assertTrue(redisCacheService.exists(key));
        
        // 验证值为null
        Object value = redisCacheService.get(key);
        assertNull(value);
    }

    @Test
    public void testHashGetNonExistentField() {
        // 测试获取不存在的哈希字段
        String hashKey = "test:hash:non:existent";
        String nonExistentField = "non:existent:field";
        
        Object value = redisCacheService.hGet(hashKey, nonExistentField);
        assertNull(value);
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 测试与真实Redis的集成
        String integrationKey = "integration:key";
        String integrationValue = "integration:value";
        
        // 清理之前的缓存
        redisCacheService.delete(integrationKey);
        
        // 测试设置和获取
        redisCacheService.set(integrationKey, integrationValue);
        String value = redisCacheService.get(integrationKey);
        assertEquals(integrationValue, value);
        
        // 测试存在性检查
        assertTrue(redisCacheService.exists(integrationKey));
        
        // 测试删除
        redisCacheService.delete(integrationKey);
        assertFalse(redisCacheService.exists(integrationKey));
    }
}
