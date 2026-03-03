package com.klzw.common.redis.exception;

import com.klzw.common.redis.constant.RedisResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisException 单元测试
 * <p>
 * 验证Redis异常类的构造方法和属性
 */
@DisplayName("Redis异常类单元测试")
class RedisExceptionTest {

    @Test
    @DisplayName("测试使用RedisResultCode构造异常")
    void testConstructor_WithResultCode() {
        // Arrange
        RedisResultCode resultCode = RedisResultCode.CACHE_GET_FAILED;

        // Act
        RedisException exception = new RedisException(resultCode);

        // Assert
        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(resultCode.getMessage(), exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertNull(exception.getCause());
    }

    @ParameterizedTest
    @EnumSource(RedisResultCode.class)
    @DisplayName("测试所有RedisResultCode都能构造异常")
    void testConstructor_WithAllResultCodes(RedisResultCode resultCode) {
        // Act
        RedisException exception = new RedisException(resultCode);

        // Assert
        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(resultCode.getMessage(), exception.getMessage());
        assertEquals("redis", exception.getModule());
    }

    @Test
    @DisplayName("测试使用RedisResultCode和自定义消息构造异常")
    void testConstructor_WithResultCodeAndMessage() {
        // Arrange
        RedisResultCode resultCode = RedisResultCode.CACHE_SET_FAILED;
        String customMessage = "自定义缓存设置失败消息";

        // Act
        RedisException exception = new RedisException(resultCode, customMessage);

        // Assert
        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(customMessage, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("测试使用RedisResultCode和异常原因构造异常")
    void testConstructor_WithResultCodeAndCause() {
        // Arrange
        RedisResultCode resultCode = RedisResultCode.CONNECTION_ERROR;
        Throwable cause = new RuntimeException("底层Redis连接失败");

        // Act
        RedisException exception = new RedisException(resultCode, cause);

        // Assert
        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(resultCode.getMessage(), exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试使用RedisResultCode、自定义消息和异常原因构造异常")
    void testConstructor_WithResultCodeMessageAndCause() {
        // Arrange
        RedisResultCode resultCode = RedisResultCode.SERIALIZATION_ERROR;
        String customMessage = "JSON序列化失败";
        Throwable cause = new IllegalArgumentException("无法序列化对象");

        // Act
        RedisException exception = new RedisException(resultCode, customMessage, cause);

        // Assert
        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(customMessage, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @ParameterizedTest
    @CsvSource({
            "900, Redis通用错误",
            "903, 缓存获取失败",
            "910, 获取锁失败",
            "920, 超过限流阈值",
            "930, 连接错误",
            "940, 序列化错误"
    })
    @DisplayName("测试使用错误码和消息构造异常（兼容旧代码）")
    void testConstructor_WithCodeAndMessage(int code, String message) {
        // Act
        RedisException exception = new RedisException(code, message);

        // Assert
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("测试使用错误码、消息和异常原因构造异常（兼容旧代码）")
    void testConstructor_WithCodeMessageAndCause() {
        // Arrange
        int code = 999;
        String message = "未知Redis错误";
        Throwable cause = new Exception("原始异常");

        // Act
        RedisException exception = new RedisException(code, message, cause);

        // Assert
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals("redis", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试缓存相关异常")
    void testCacheExceptions() {
        // 测试各种缓存异常
        RedisException getException = new RedisException(RedisResultCode.CACHE_GET_FAILED);
        assertEquals(903, getException.getCode());
        assertEquals("缓存获取失败", getException.getMessage());

        RedisException setException = new RedisException(RedisResultCode.CACHE_SET_FAILED, "设置key失败");
        assertEquals(904, setException.getCode());
        assertEquals("设置key失败", setException.getMessage());

        RedisException deleteException = new RedisException(RedisResultCode.CACHE_DELETE_FAILED);
        assertEquals(905, deleteException.getCode());

        RedisException expireException = new RedisException(RedisResultCode.CACHE_EXPIRE_FAILED);
        assertEquals(906, expireException.getCode());
    }

    @Test
    @DisplayName("测试分布式锁相关异常")
    void testLockExceptions() {
        // 测试各种锁异常
        RedisException acquireException = new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED);
        assertEquals(910, acquireException.getCode());
        assertEquals("获取锁失败", acquireException.getMessage());

        RedisException releaseException = new RedisException(RedisResultCode.LOCK_RELEASE_FAILED);
        assertEquals(911, releaseException.getCode());

        RedisException expiredException = new RedisException(RedisResultCode.LOCK_EXPIRED);
        assertEquals(912, expiredException.getCode());

        RedisException notOwnerException = new RedisException(RedisResultCode.LOCK_NOT_OWNER);
        assertEquals(913, notOwnerException.getCode());

        RedisException timeoutException = new RedisException(RedisResultCode.LOCK_TIMEOUT);
        assertEquals(914, timeoutException.getCode());
    }

    @Test
    @DisplayName("测试限流相关异常")
    void testRateLimitExceptions() {
        RedisException exceededException = new RedisException(RedisResultCode.RATE_LIMIT_EXCEEDED);
        assertEquals(920, exceededException.getCode());
        assertEquals("超过限流阈值", exceededException.getMessage());

        RedisException configException = new RedisException(RedisResultCode.RATE_LIMIT_CONFIG_ERROR);
        assertEquals(921, configException.getCode());

        RedisException resetException = new RedisException(RedisResultCode.RATE_LIMIT_RESET_FAILED);
        assertEquals(922, resetException.getCode());
    }

    @Test
    @DisplayName("测试连接相关异常")
    void testConnectionExceptions() {
        Throwable connectionCause = new RuntimeException("Connection refused");

        RedisException connectionException = new RedisException(RedisResultCode.CONNECTION_ERROR, connectionCause);
        assertEquals(930, connectionException.getCode());
        assertEquals(connectionCause, connectionException.getCause());

        RedisException timeoutException = new RedisException(RedisResultCode.CONNECTION_TIMEOUT);
        assertEquals(931, timeoutException.getCode());

        RedisException closedException = new RedisException(RedisResultCode.CONNECTION_CLOSED);
        assertEquals(932, closedException.getCode());

        RedisException poolExhaustedException = new RedisException(RedisResultCode.CONNECTION_POOL_EXHAUSTED);
        assertEquals(933, poolExhaustedException.getCode());
    }

    @Test
    @DisplayName("测试序列化相关异常")
    void testSerializationExceptions() {
        Throwable serializationCause = new IllegalStateException("Object cannot be serialized");

        RedisException serializationException = new RedisException(
                RedisResultCode.SERIALIZATION_ERROR, "序列化失败", serializationCause);
        assertEquals(940, serializationException.getCode());
        assertEquals("序列化失败", serializationException.getMessage());
        assertEquals(serializationCause, serializationException.getCause());

        RedisException deserializationException = new RedisException(RedisResultCode.DESERIALIZATION_ERROR);
        assertEquals(941, deserializationException.getCode());

        RedisException notFoundException = new RedisException(RedisResultCode.SERIALIZER_NOT_FOUND);
        assertEquals(942, notFoundException.getCode());
    }

    @Test
    @DisplayName("测试异常继承关系")
    void testExceptionInheritance() {
        // Arrange
        RedisException exception = new RedisException(RedisResultCode.REDIS_ERROR);

        // Assert
        assertTrue(exception instanceof RuntimeException, "RedisException应该是RuntimeException的子类");
        assertTrue(exception instanceof com.klzw.common.core.exception.BaseException,
                "RedisException应该是BaseException的子类");
    }

    @Test
    @DisplayName("测试异常消息格式")
    void testExceptionMessageFormat() {
        // Arrange
        String customMessage = "操作失败: key=test:123";
        RedisException exception = new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, customMessage);

        // Assert
        assertEquals(customMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("操作失败"));
        assertTrue(exception.getMessage().contains("key=test:123"));
    }

    @Test
    @DisplayName("测试异常链")
    void testExceptionChaining() {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("Invalid argument");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);
        RedisException redisException = new RedisException(
                RedisResultCode.COMMAND_EXECUTION_ERROR, "Redis命令执行失败", intermediateCause);

        // Assert
        assertEquals(intermediateCause, redisException.getCause());
        assertEquals(rootCause, redisException.getCause().getCause());
    }

    @Test
    @DisplayName("测试通用Redis异常")
    void testGeneralRedisException() {
        RedisException generalException = new RedisException(RedisResultCode.REDIS_ERROR);
        assertEquals(900, generalException.getCode());
        assertEquals("Redis操作失败", generalException.getMessage());
        assertEquals("redis", generalException.getModule());
    }
}
