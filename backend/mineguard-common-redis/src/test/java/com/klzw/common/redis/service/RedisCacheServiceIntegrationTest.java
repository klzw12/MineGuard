package com.klzw.common.redis.service;

import com.klzw.common.redis.AbstractRedisIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 缓存服务集成测试
 */
@DisplayName("Redis缓存服务集成测试")
@Tag("integration")
public class RedisCacheServiceIntegrationTest extends AbstractRedisIntegrationTest {

    @Autowired
    private RedisCacheService redisCacheService;

    private static final String TEST_KEY_PREFIX = "test:";

    @BeforeEach
    void setUp() {
        cleanupTestKeys();
    }

    @AfterEach
    void tearDown() {
        cleanupTestKeys();
    }

    private void cleanupTestKeys() {
        redisCacheService.delete(TEST_KEY_PREFIX + "string");
        redisCacheService.delete(TEST_KEY_PREFIX + "object");
        redisCacheService.delete(TEST_KEY_PREFIX + "expire");
        redisCacheService.delete(TEST_KEY_PREFIX + "counter");
        redisCacheService.delete(TEST_KEY_PREFIX + "hash");
        redisCacheService.deleteBatch(Arrays.asList(
            TEST_KEY_PREFIX + "batch1",
            TEST_KEY_PREFIX + "batch2",
            TEST_KEY_PREFIX + "batch3"
        ));
    }

    @Test
    @DisplayName("设置和获取字符串缓存")
    void testSetAndGetString() {
        String key = TEST_KEY_PREFIX + "string";
        String value = "test_value";
        
        redisCacheService.set(key, value);
        String result = redisCacheService.get(key);
        
        assertEquals(value, result);
    }

    @Test
    @DisplayName("设置和获取对象缓存")
    void testSetAndGetObject() {
        String key = TEST_KEY_PREFIX + "object";
        TestUser user = new TestUser(1L, "张三", "zhangsan@example.com");
        
        redisCacheService.set(key, user);
        TestUser result = redisCacheService.get(key);
        
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("设置带过期时间的缓存")
    void testSetWithExpire() throws InterruptedException {
        String key = TEST_KEY_PREFIX + "expire";
        String value = "expire_value";
        
        redisCacheService.set(key, value, 2, TimeUnit.SECONDS);
        
        assertTrue(redisCacheService.exists(key));
        assertEquals(value, redisCacheService.get(key));
        
        Thread.sleep(2100);
        
        assertFalse(redisCacheService.exists(key));
        assertNull(redisCacheService.get(key));
    }

    @Test
    @DisplayName("批量设置和获取缓存")
    void testSetAndGetBatch() {
        Map<String, Object> map = new HashMap<>();
        map.put(TEST_KEY_PREFIX + "batch1", "value1");
        map.put(TEST_KEY_PREFIX + "batch2", "value2");
        map.put(TEST_KEY_PREFIX + "batch3", "value3");
        
        redisCacheService.setBatch(map);
        
        Map<String, String> result = redisCacheService.getBatch(map.keySet());
        
        assertEquals(3, result.size());
        assertEquals("value1", result.get(TEST_KEY_PREFIX + "batch1"));
        assertEquals("value2", result.get(TEST_KEY_PREFIX + "batch2"));
        assertEquals("value3", result.get(TEST_KEY_PREFIX + "batch3"));
    }

    @Test
    @DisplayName("删除缓存")
    void testDelete() {
        String key = TEST_KEY_PREFIX + "string";
        redisCacheService.set(key, "value");
        
        assertTrue(redisCacheService.exists(key));
        redisCacheService.delete(key);
        assertFalse(redisCacheService.exists(key));
    }

    @Test
    @DisplayName("批量删除缓存")
    void testDeleteBatch() {
        Map<String, Object> map = new HashMap<>();
        map.put(TEST_KEY_PREFIX + "batch1", "value1");
        map.put(TEST_KEY_PREFIX + "batch2", "value2");
        map.put(TEST_KEY_PREFIX + "batch3", "value3");
        redisCacheService.setBatch(map);
        
        Long deleted = redisCacheService.deleteBatch(map.keySet());
        
        assertEquals(3L, deleted);
    }

    @Test
    @DisplayName("检查缓存是否存在")
    void testExists() {
        String key = TEST_KEY_PREFIX + "string";
        
        assertFalse(redisCacheService.exists(key));
        
        redisCacheService.set(key, "value");
        assertTrue(redisCacheService.exists(key));
    }

    @Test
    @DisplayName("批量检查缓存是否存在")
    void testExistsBatch() {
        Map<String, Object> map = new HashMap<>();
        map.put(TEST_KEY_PREFIX + "batch1", "value1");
        map.put(TEST_KEY_PREFIX + "batch2", "value2");
        redisCacheService.setBatch(map);
        
        Map<String, Boolean> result = redisCacheService.existsBatch(Arrays.asList(
            TEST_KEY_PREFIX + "batch1",
            TEST_KEY_PREFIX + "batch2",
            TEST_KEY_PREFIX + "batch3"
        ));
        
        assertEquals(3, result.size());
        assertTrue(result.get(TEST_KEY_PREFIX + "batch1"));
        assertTrue(result.get(TEST_KEY_PREFIX + "batch2"));
        assertFalse(result.get(TEST_KEY_PREFIX + "batch3"));
    }

    @Test
    @DisplayName("设置缓存过期时间")
    void testExpire() throws InterruptedException {
        String key = TEST_KEY_PREFIX + "expire";
        redisCacheService.set(key, "value");
        
        Boolean result = redisCacheService.expire(key, 10, TimeUnit.SECONDS);
        assertTrue(result);
        
        Long ttl = redisCacheService.getExpire(key, TimeUnit.SECONDS);
        assertTrue(ttl > 0 && ttl <= 10);
    }

    @Test
    @DisplayName("自增操作")
    void testIncrement() {
        String key = TEST_KEY_PREFIX + "counter";
        
        Long result1 = redisCacheService.increment(key);
        assertEquals(1L, result1);
        
        Long result2 = redisCacheService.increment(key);
        assertEquals(2L, result2);
        
        Long result3 = redisCacheService.increment(key, 5);
        assertEquals(7L, result3);
    }

    @Test
    @DisplayName("自减操作")
    void testDecrement() {
        String key = TEST_KEY_PREFIX + "counter";
        redisCacheService.set(key, 10);
        
        Long result1 = redisCacheService.decrement(key);
        assertEquals(9L, result1);
        
        Long result2 = redisCacheService.decrement(key, 3);
        assertEquals(6L, result2);
    }

    @Test
    @DisplayName("哈希操作")
    void testHashOperations() {
        String key = TEST_KEY_PREFIX + "hash";
        
        redisCacheService.hSet(key, "field1", "value1");
        redisCacheService.hSet(key, "field2", "value2");
        
        assertEquals("value1", redisCacheService.hGet(key, "field1"));
        assertEquals("value2", redisCacheService.hGet(key, "field2"));
        
        Map<String, Object> map = new HashMap<>();
        map.put("field3", "value3");
        map.put("field4", "value4");
        redisCacheService.hSetBatch(key, map);
        
        Map<String, String> result = redisCacheService.hGetBatch(key, Arrays.asList("field1", "field3"));
        assertEquals(2, result.size());
        
        Long deleted = redisCacheService.hDelete(key, "field1", "field2");
        assertEquals(2L, deleted);
    }

    @Test
    @DisplayName("空值处理")
    void testNullHandling() {
        String key = TEST_KEY_PREFIX + "null";
        
        assertNull(redisCacheService.get(key));
        
        Map<String, String> result = redisCacheService.getBatch(Collections.singletonList(key));
        assertTrue(result.isEmpty());
        
        assertEquals(0L, redisCacheService.deleteBatch(Collections.emptyList()));
    }

    static class TestUser {
        private Long id;
        private String name;
        private String email;

        public TestUser() {}

        public TestUser(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
