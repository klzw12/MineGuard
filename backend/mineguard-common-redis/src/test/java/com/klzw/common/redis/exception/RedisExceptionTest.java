package com.klzw.common.redis.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 异常类单元测试
 */
@DisplayName("Redis异常类测试")
public class RedisExceptionTest {

    @Test
    @DisplayName("创建基本异常")
    void testCreateBasicException() {
        String message = "Redis操作失败";
        RedisException exception = new RedisException(message);
        
        assertEquals(RedisException.REDIS_COMMAND_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
    }

    @Test
    @DisplayName("创建带原因的异常")
    void testCreateExceptionWithCause() {
        String message = "Redis操作失败";
        Throwable cause = new RuntimeException("连接超时");
        RedisException exception = new RedisException(message, cause);
        
        assertEquals(RedisException.REDIS_COMMAND_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建带错误码和消息的异常")
    void testCreateExceptionWithCodeAndMessage() {
        int code = 900;
        String message = "连接失败";
        RedisException exception = new RedisException(code, message);
        
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
    }

    @Test
    @DisplayName("创建带错误码、消息和原因的异常")
    void testCreateExceptionWithCodeMessageAndCause() {
        int code = 901;
        String message = "序列化失败";
        Throwable cause = new RuntimeException("JSON转换错误");
        RedisException exception = new RedisException(code, message, cause);
        
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建连接错误异常")
    void testConnectionError() {
        String message = "无法连接到Redis服务器";
        RedisException exception = RedisException.connectionError(message);
        
        assertEquals(RedisException.REDIS_CONNECTION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的连接错误异常")
    void testConnectionErrorWithCause() {
        String message = "无法连接到Redis服务器";
        Throwable cause = new RuntimeException("Connection refused");
        RedisException exception = RedisException.connectionError(message, cause);
        
        assertEquals(RedisException.REDIS_CONNECTION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建序列化错误异常")
    void testSerializationError() {
        String message = "对象序列化失败";
        RedisException exception = RedisException.serializationError(message);
        
        assertEquals(RedisException.REDIS_SERIALIZATION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的序列化错误异常")
    void testSerializationErrorWithCause() {
        String message = "对象序列化失败";
        Throwable cause = new RuntimeException("Not serializable");
        RedisException exception = RedisException.serializationError(message, cause);
        
        assertEquals(RedisException.REDIS_SERIALIZATION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建反序列化错误异常")
    void testDeserializationError() {
        String message = "对象反序列化失败";
        RedisException exception = RedisException.deserializationError(message);
        
        assertEquals(RedisException.REDIS_DESERIALIZATION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的反序列化错误异常")
    void testDeserializationErrorWithCause() {
        String message = "对象反序列化失败";
        Throwable cause = new RuntimeException("Invalid JSON");
        RedisException exception = RedisException.deserializationError(message, cause);
        
        assertEquals(RedisException.REDIS_DESERIALIZATION_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建锁获取错误异常")
    void testLockAcquireError() {
        String message = "获取分布式锁失败";
        RedisException exception = RedisException.lockAcquireError(message);
        
        assertEquals(RedisException.REDIS_LOCK_ACQUIRE_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的锁获取错误异常")
    void testLockAcquireErrorWithCause() {
        String message = "获取分布式锁失败";
        Throwable cause = new RuntimeException("Lock timeout");
        RedisException exception = RedisException.lockAcquireError(message, cause);
        
        assertEquals(RedisException.REDIS_LOCK_ACQUIRE_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建锁释放错误异常")
    void testLockReleaseError() {
        String message = "释放分布式锁失败";
        RedisException exception = RedisException.lockReleaseError(message);
        
        assertEquals(RedisException.REDIS_LOCK_RELEASE_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的锁释放错误异常")
    void testLockReleaseErrorWithCause() {
        String message = "释放分布式锁失败";
        Throwable cause = new RuntimeException("Lock not held");
        RedisException exception = RedisException.lockReleaseError(message, cause);
        
        assertEquals(RedisException.REDIS_LOCK_RELEASE_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建限流错误异常")
    void testRateLimitError() {
        String message = "请求过于频繁";
        RedisException exception = RedisException.rateLimitError(message);
        
        assertEquals(RedisException.REDIS_RATE_LIMIT_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的限流错误异常")
    void testRateLimitErrorWithCause() {
        String message = "请求过于频繁";
        Throwable cause = new RuntimeException("Rate limit exceeded");
        RedisException exception = RedisException.rateLimitError(message, cause);
        
        assertEquals(RedisException.REDIS_RATE_LIMIT_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建命令执行错误异常")
    void testCommandError() {
        String message = "Redis命令执行失败";
        RedisException exception = RedisException.commandError(message);
        
        assertEquals(RedisException.REDIS_COMMAND_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的命令执行错误异常")
    void testCommandErrorWithCause() {
        String message = "Redis命令执行失败";
        Throwable cause = new RuntimeException("Invalid command");
        RedisException exception = RedisException.commandError(message, cause);
        
        assertEquals(RedisException.REDIS_COMMAND_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("创建超时错误异常")
    void testTimeoutError() {
        String message = "Redis操作超时";
        RedisException exception = RedisException.timeoutError(message);
        
        assertEquals(RedisException.REDIS_TIMEOUT_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("创建带原因的超时错误异常")
    void testTimeoutErrorWithCause() {
        String message = "Redis操作超时";
        Throwable cause = new RuntimeException("Timeout after 3000ms");
        RedisException exception = RedisException.timeoutError(message, cause);
        
        assertEquals(RedisException.REDIS_TIMEOUT_ERROR, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("错误码常量验证")
    void testErrorCodes() {
        assertEquals(900, RedisException.REDIS_CONNECTION_ERROR);
        assertEquals(901, RedisException.REDIS_SERIALIZATION_ERROR);
        assertEquals(902, RedisException.REDIS_DESERIALIZATION_ERROR);
        assertEquals(903, RedisException.REDIS_LOCK_ACQUIRE_ERROR);
        assertEquals(904, RedisException.REDIS_LOCK_RELEASE_ERROR);
        assertEquals(905, RedisException.REDIS_RATE_LIMIT_ERROR);
        assertEquals(906, RedisException.REDIS_COMMAND_ERROR);
        assertEquals(907, RedisException.REDIS_TIMEOUT_ERROR);
    }
}
