package com.klzw.common.redis.handler;

import com.klzw.common.core.result.Result;
import com.klzw.common.redis.exception.RedisException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisExceptionHandlerStrategy 测试类
 */
@SpringBootTest
public class RedisExceptionHandlerStrategyTest {

    @Autowired
    private RedisExceptionHandlerStrategy redisExceptionHandlerStrategy;

    @Test
    public void testSupport() {
        // 测试支持 RedisException
        RedisException redisException = new RedisException(RedisException.REDIS_CONNECTION_ERROR, "Connection error");
        assertTrue(redisExceptionHandlerStrategy.support(redisException));
        
        // 测试不支持其他异常
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        assertFalse(redisExceptionHandlerStrategy.support(runtimeException));
    }

    @Test
    public void testHandleRedisConnectionError() {
        // 测试处理 Redis 连接错误
        RedisException redisException = new RedisException(RedisException.REDIS_CONNECTION_ERROR, "Connection error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_CONNECTION_ERROR, result.getCode());
        assertEquals("Connection error", result.getMessage());
    }

    @Test
    public void testHandleRedisSerializationError() {
        // 测试处理 Redis 序列化错误
        RedisException redisException = new RedisException(RedisException.REDIS_SERIALIZATION_ERROR, "Serialization error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_SERIALIZATION_ERROR, result.getCode());
        assertEquals("Serialization error", result.getMessage());
    }

    @Test
    public void testHandleRedisDeserializationError() {
        // 测试处理 Redis 反序列化错误
        RedisException redisException = new RedisException(RedisException.REDIS_DESERIALIZATION_ERROR, "Deserialization error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_DESERIALIZATION_ERROR, result.getCode());
        assertEquals("Deserialization error", result.getMessage());
    }

    @Test
    public void testHandleRedisLockAcquireError() {
        // 测试处理 Redis 锁获取错误
        RedisException redisException = new RedisException(RedisException.REDIS_LOCK_ACQUIRE_ERROR, "Lock acquire error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_LOCK_ACQUIRE_ERROR, result.getCode());
        assertEquals("Lock acquire error", result.getMessage());
    }

    @Test
    public void testHandleRedisLockReleaseError() {
        // 测试处理 Redis 锁释放错误
        RedisException redisException = new RedisException(RedisException.REDIS_LOCK_RELEASE_ERROR, "Lock release error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_LOCK_RELEASE_ERROR, result.getCode());
        assertEquals("Lock release error", result.getMessage());
    }

    @Test
    public void testHandleRedisRateLimitError() {
        // 测试处理 Redis 限流错误
        RedisException redisException = new RedisException(RedisException.REDIS_RATE_LIMIT_ERROR, "Rate limit error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_RATE_LIMIT_ERROR, result.getCode());
        assertEquals("Rate limit error", result.getMessage());
    }

    @Test
    public void testHandleRedisCommandError() {
        // 测试处理 Redis 命令执行错误
        RedisException redisException = new RedisException(RedisException.REDIS_COMMAND_ERROR, "Command error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_COMMAND_ERROR, result.getCode());
        assertEquals("Command error", result.getMessage());
    }

    @Test
    public void testHandleRedisTimeoutError() {
        // 测试处理 Redis 超时错误
        RedisException redisException = new RedisException(RedisException.REDIS_TIMEOUT_ERROR, "Timeout error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(RedisException.REDIS_TIMEOUT_ERROR, result.getCode());
        assertEquals("Timeout error", result.getMessage());
    }

    @Test
    public void testHandleRedisUnknownError() {
        // 测试处理 Redis 未知错误
        RedisException redisException = new RedisException(9999, "Unknown error");
        Result<?> result = redisExceptionHandlerStrategy.handle(redisException);
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(9999, result.getCode());
        assertEquals("Unknown error", result.getMessage());
    }
}
