package com.klzw.common.redis.handler;

import com.klzw.common.core.result.Result;
import com.klzw.common.redis.exception.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 异常处理策略单元测试
 */
@DisplayName("Redis异常处理策略测试")
public class RedisExceptionHandlerStrategyTest {

    private RedisExceptionHandlerStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RedisExceptionHandlerStrategy();
    }

    @Test
    @DisplayName("支持RedisException异常")
    void testSupportRedisException() {
        RedisException exception = new RedisException("测试异常");
        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("不支持其他异常")
    void testNotSupportOtherException() {
        RuntimeException exception = new RuntimeException("其他异常");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("不支持NullPointerException")
    void testNotSupportNullPointerException() {
        NullPointerException exception = new NullPointerException("空指针");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("处理连接错误异常")
    void testHandleConnectionError() {
        RedisException exception = RedisException.connectionError("连接失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_CONNECTION_ERROR, result.getCode());
        assertEquals("连接失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理序列化错误异常")
    void testHandleSerializationError() {
        RedisException exception = RedisException.serializationError("序列化失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_SERIALIZATION_ERROR, result.getCode());
        assertEquals("序列化失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理反序列化错误异常")
    void testHandleDeserializationError() {
        RedisException exception = RedisException.deserializationError("反序列化失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_DESERIALIZATION_ERROR, result.getCode());
        assertEquals("反序列化失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理锁获取错误异常")
    void testHandleLockAcquireError() {
        RedisException exception = RedisException.lockAcquireError("获取锁失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_LOCK_ACQUIRE_ERROR, result.getCode());
        assertEquals("获取锁失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理锁释放错误异常")
    void testHandleLockReleaseError() {
        RedisException exception = RedisException.lockReleaseError("释放锁失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_LOCK_RELEASE_ERROR, result.getCode());
        assertEquals("释放锁失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理限流错误异常")
    void testHandleRateLimitError() {
        RedisException exception = RedisException.rateLimitError("请求过于频繁");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_RATE_LIMIT_ERROR, result.getCode());
        assertEquals("请求过于频繁", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理命令执行错误异常")
    void testHandleCommandError() {
        RedisException exception = RedisException.commandError("命令执行失败");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_COMMAND_ERROR, result.getCode());
        assertEquals("命令执行失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理超时错误异常")
    void testHandleTimeoutError() {
        RedisException exception = RedisException.timeoutError("操作超时");
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_TIMEOUT_ERROR, result.getCode());
        assertEquals("操作超时", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理带原因的异常")
    void testHandleExceptionWithCause() {
        Throwable cause = new RuntimeException("底层错误");
        RedisException exception = RedisException.connectionError("连接失败", cause);
        Result<?> result = strategy.handle(exception);
        
        assertEquals(RedisException.REDIS_CONNECTION_ERROR, result.getCode());
        assertEquals("连接失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理自定义错误码异常")
    void testHandleCustomCodeException() {
        int customCode = 999;
        String message = "自定义错误";
        RedisException exception = new RedisException(customCode, message);
        Result<?> result = strategy.handle(exception);
        
        assertEquals(customCode, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }
}
