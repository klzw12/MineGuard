package com.klzw.common.redis.service;

import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

/**
 * RedisCacheService 单元测试
 * <p>
 * 使用Mockito模拟RedisTemplate，测试缓存服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Redis缓存服务单元测试")
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("测试设置缓存 - 成功")
    void testSet_Success() {
        // Arrange
        String key = "test:key";
        String value = "testValue";
        long expire = 3600;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // Act
        redisCacheService.set(key, value, expire, timeUnit);

        // Assert
        verify(valueOperations).set(key, value, expire, timeUnit);
    }

    @Test
    @DisplayName("测试设置缓存 - 使用默认值")
    void testSet_WithDefaultExpire() {
        // Arrange
        String key = "test:key";
        String value = "testValue";

        // Act
        redisCacheService.set(key, value);

        // Assert
        verify(valueOperations).set(key, value, 1, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("测试设置缓存 - 异常时抛出RedisException")
    void testSet_Exception() {
        // Arrange
        String key = "test:key";
        String value = "testValue";
        doThrow(new RuntimeException("Redis连接失败"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // Act & Assert
        RedisException exception = assertThrows(RedisException.class, () ->
                redisCacheService.set(key, value, 3600, TimeUnit.SECONDS));

        assertEquals(RedisResultCode.CACHE_SET_FAILED.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("缓存设置失败"));
    }

    @Test
    @DisplayName("测试获取缓存 - 成功")
    void testGet_Success() {
        // Arrange
        String key = "test:key";
        String expectedValue = "testValue";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // Act
        String result = redisCacheService.get(key);

        // Assert
        assertEquals(expectedValue, result);
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("测试获取缓存 - 键不存在返回null")
    void testGet_NotFound() {
        // Arrange
        String key = "test:key:notexist";
        when(valueOperations.get(key)).thenReturn(null);

        // Act
        String result = redisCacheService.get(key);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("测试获取缓存 - 异常时抛出RedisException")
    void testGet_Exception() {
        // Arrange
        String key = "test:key";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis连接失败"));

        // Act & Assert
        RedisException exception = assertThrows(RedisException.class, () ->
                redisCacheService.get(key));

        assertEquals(RedisResultCode.CACHE_GET_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试批量获取缓存 - 成功")
    void testGetBatch_Success() {
        // Arrange
        List<String> keys = Arrays.asList("key1", "key2", "key3");
        List<Object> values = Arrays.asList("value1", "value2", "value3");
        when(valueOperations.multiGet(keys)).thenReturn(values);

        // Act
        Map<String, String> result = redisCacheService.getBatch(keys);

        // Assert
        assertEquals(3, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        assertEquals("value3", result.get("key3"));
    }

    @Test
    @DisplayName("测试批量获取缓存 - 空列表返回空Map")
    void testGetBatch_EmptyList() {
        // Act
        Map<String, String> result = redisCacheService.getBatch(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
        verify(valueOperations, never()).multiGet(anyList());
    }

    @Test
    @DisplayName("测试批量获取缓存 - null列表返回空Map")
    void testGetBatch_NullList() {
        // Act
        Map<String, String> result = redisCacheService.getBatch(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(valueOperations, never()).multiGet(anyList());
    }

    @Test
    @DisplayName("测试删除缓存 - 成功")
    void testDelete_Success() {
        // Arrange
        String key = "test:key";

        // Act
        redisCacheService.delete(key);

        // Assert
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("测试删除缓存 - 异常时抛出RedisException")
    void testDelete_Exception() {
        // Arrange
        String key = "test:key";
        doThrow(new RuntimeException("Redis连接失败")).when(redisTemplate).delete(anyString());

        // Act & Assert
        RedisException exception = assertThrows(RedisException.class, () ->
                redisCacheService.delete(key));

        assertEquals(RedisResultCode.CACHE_DELETE_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试批量删除缓存 - 成功")
    void testDeleteBatch_Success() {
        // Arrange
        List<String> keys = Arrays.asList("key1", "key2");
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // Act
        Long result = redisCacheService.deleteBatch(keys);

        // Assert
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("测试批量删除缓存 - 空列表返回0")
    void testDeleteBatch_EmptyList() {
        // Act
        Long result = redisCacheService.deleteBatch(Collections.emptyList());

        // Assert
        assertEquals(0L, result);
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("测试检查缓存是否存在 - 存在")
    void testExists_True() {
        // Arrange
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // Act
        Boolean result = redisCacheService.exists(key);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查缓存是否存在 - 不存在")
    void testExists_False() {
        // Arrange
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // Act
        Boolean result = redisCacheService.exists(key);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试设置过期时间 - 成功")
    void testExpire_Success() {
        // Arrange
        String key = "test:key";
        long expire = 3600;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        when(redisTemplate.expire(key, expire, timeUnit)).thenReturn(true);

        // Act
        Boolean result = redisCacheService.expire(key, expire, timeUnit);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试设置过期时间 - 异常时抛出RedisException")
    void testExpire_Exception() {
        // Arrange
        String key = "test:key";
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException("Redis连接失败"));

        // Act & Assert
        RedisException exception = assertThrows(RedisException.class, () ->
                redisCacheService.expire(key, 3600, TimeUnit.SECONDS));

        assertEquals(RedisResultCode.CACHE_EXPIRE_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试获取过期时间 - 成功")
    void testGetExpire_Success() {
        // Arrange
        String key = "test:key";
        long expectedExpire = 3600;
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(expectedExpire);

        // Act
        Long result = redisCacheService.getExpire(key, TimeUnit.SECONDS);

        // Assert
        assertEquals(expectedExpire, result);
    }

    @Test
    @DisplayName("测试自增 - 成功")
    void testIncrement_Success() {
        // Arrange
        String key = "test:counter";
        long expectedValue = 1L;
        when(valueOperations.increment(key)).thenReturn(expectedValue);

        // Act
        Long result = redisCacheService.increment(key);

        // Assert
        assertEquals(expectedValue, result);
    }

    @Test
    @DisplayName("测试自增指定值 - 成功")
    void testIncrementWithDelta_Success() {
        // Arrange
        String key = "test:counter";
        long delta = 5;
        long expectedValue = 10L;
        when(valueOperations.increment(key, delta)).thenReturn(expectedValue);

        // Act
        Long result = redisCacheService.increment(key, delta);

        // Assert
        assertEquals(expectedValue, result);
    }

    @Test
    @DisplayName("测试自减 - 成功")
    void testDecrement_Success() {
        // Arrange
        String key = "test:counter";
        long expectedValue = -1L;
        when(valueOperations.decrement(key)).thenReturn(expectedValue);

        // Act
        Long result = redisCacheService.decrement(key);

        // Assert
        assertEquals(expectedValue, result);
    }

    @Test
    @DisplayName("测试根据模式删除缓存 - 成功")
    void testDeleteByPattern_Success() {
        // Arrange
        String pattern = "test:*";
        Set<String> keys = new HashSet<>(Arrays.asList("test:key1", "test:key2"));
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // Act
        Long result = redisCacheService.deleteByPattern(pattern);

        // Assert
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("测试根据模式删除缓存 - 无匹配键")
    void testDeleteByPattern_NoMatch() {
        // Arrange
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(Collections.emptySet());

        // Act
        Long result = redisCacheService.deleteByPattern(pattern);

        // Assert
        assertEquals(0L, result);
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("测试根据模式删除缓存 - 异常时抛出RedisException")
    void testDeleteByPattern_Exception() {
        // Arrange
        String pattern = "test:*";
        when(redisTemplate.keys(anyString())).thenThrow(new RuntimeException("Redis连接失败"));

        // Act & Assert
        RedisException exception = assertThrows(RedisException.class, () ->
                redisCacheService.deleteByPattern(pattern));

        assertEquals(RedisResultCode.CACHE_DELETE_FAILED.getCode(), exception.getCode());
    }
}
