package com.klzw.common.redis.handler;

import com.klzw.common.core.result.Result;
import com.klzw.common.redis.constant.RedisResultCode;
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
        RedisException exception = new RedisException(RedisResultCode.REDIS_ERROR);
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
    @DisplayName("处理通用Redis错误")
    void testHandleGeneralRedisError() {
        RedisException exception = new RedisException(RedisResultCode.REDIS_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.REDIS_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.REDIS_ERROR.getMessage(), result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("处理缓存未命中错误")
    void testHandleCacheMissError() {
        RedisException exception = new RedisException(RedisResultCode.CACHE_MISS);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CACHE_MISS.getCode(), result.getCode());
        assertEquals(RedisResultCode.CACHE_MISS.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理缓存获取失败错误")
    void testHandleCacheGetFailedError() {
        RedisException exception = new RedisException(RedisResultCode.CACHE_GET_FAILED, "获取缓存失败: test:key");
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CACHE_GET_FAILED.getCode(), result.getCode());
        assertEquals("获取缓存失败: test:key", result.getMessage());
    }

    @Test
    @DisplayName("处理缓存设置失败错误")
    void testHandleCacheSetFailedError() {
        RedisException exception = new RedisException(RedisResultCode.CACHE_SET_FAILED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CACHE_SET_FAILED.getCode(), result.getCode());
        assertEquals(RedisResultCode.CACHE_SET_FAILED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理缓存删除失败错误")
    void testHandleCacheDeleteFailedError() {
        RedisException exception = new RedisException(RedisResultCode.CACHE_DELETE_FAILED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CACHE_DELETE_FAILED.getCode(), result.getCode());
        assertEquals(RedisResultCode.CACHE_DELETE_FAILED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理获取锁失败错误")
    void testHandleLockAcquireFailedError() {
        RedisException exception = new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.LOCK_ACQUIRE_FAILED.getCode(), result.getCode());
        assertEquals(RedisResultCode.LOCK_ACQUIRE_FAILED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理释放锁失败错误")
    void testHandleLockReleaseFailedError() {
        RedisException exception = new RedisException(RedisResultCode.LOCK_RELEASE_FAILED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.LOCK_RELEASE_FAILED.getCode(), result.getCode());
        assertEquals(RedisResultCode.LOCK_RELEASE_FAILED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理锁已过期错误")
    void testHandleLockExpiredError() {
        RedisException exception = new RedisException(RedisResultCode.LOCK_EXPIRED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.LOCK_EXPIRED.getCode(), result.getCode());
        assertEquals(RedisResultCode.LOCK_EXPIRED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理超过限流阈值错误")
    void testHandleRateLimitExceededError() {
        RedisException exception = new RedisException(RedisResultCode.RATE_LIMIT_EXCEEDED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.RATE_LIMIT_EXCEEDED.getCode(), result.getCode());
        assertEquals(RedisResultCode.RATE_LIMIT_EXCEEDED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理限流配置错误")
    void testHandleRateLimitConfigError() {
        RedisException exception = new RedisException(RedisResultCode.RATE_LIMIT_CONFIG_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理连接错误")
    void testHandleConnectionError() {
        RedisException exception = new RedisException(RedisResultCode.CONNECTION_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CONNECTION_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.CONNECTION_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理连接超时错误")
    void testHandleConnectionTimeoutError() {
        RedisException exception = new RedisException(RedisResultCode.CONNECTION_TIMEOUT);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CONNECTION_TIMEOUT.getCode(), result.getCode());
        assertEquals(RedisResultCode.CONNECTION_TIMEOUT.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理序列化错误")
    void testHandleSerializationError() {
        RedisException exception = new RedisException(RedisResultCode.SERIALIZATION_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.SERIALIZATION_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.SERIALIZATION_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理反序列化错误")
    void testHandleDeserializationError() {
        RedisException exception = new RedisException(RedisResultCode.DESERIALIZATION_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.DESERIALIZATION_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.DESERIALIZATION_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理命令执行错误")
    void testHandleCommandExecutionError() {
        RedisException exception = new RedisException(RedisResultCode.COMMAND_EXECUTION_ERROR);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.COMMAND_EXECUTION_ERROR.getCode(), result.getCode());
        assertEquals(RedisResultCode.COMMAND_EXECUTION_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理带原因的异常")
    void testHandleExceptionWithCause() {
        Throwable cause = new RuntimeException("底层错误");
        RedisException exception = new RedisException(RedisResultCode.CONNECTION_ERROR, "连接失败", cause);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CONNECTION_ERROR.getCode(), result.getCode());
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
    }

    @Test
    @DisplayName("处理连接池耗尽错误")
    void testHandleConnectionPoolExhaustedError() {
        RedisException exception = new RedisException(RedisResultCode.CONNECTION_POOL_EXHAUSTED);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.CONNECTION_POOL_EXHAUSTED.getCode(), result.getCode());
        assertEquals(RedisResultCode.CONNECTION_POOL_EXHAUSTED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理锁非所有者错误")
    void testHandleLockNotOwnerError() {
        RedisException exception = new RedisException(RedisResultCode.LOCK_NOT_OWNER);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.LOCK_NOT_OWNER.getCode(), result.getCode());
        assertEquals(RedisResultCode.LOCK_NOT_OWNER.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理锁超时错误")
    void testHandleLockTimeoutError() {
        RedisException exception = new RedisException(RedisResultCode.LOCK_TIMEOUT);
        Result<?> result = strategy.handle(exception);

        assertEquals(RedisResultCode.LOCK_TIMEOUT.getCode(), result.getCode());
        assertEquals(RedisResultCode.LOCK_TIMEOUT.getMessage(), result.getMessage());
    }
}
