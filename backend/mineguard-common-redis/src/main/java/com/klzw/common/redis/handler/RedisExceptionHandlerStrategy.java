package com.klzw.common.redis.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis 异常处理策略
 * <p>
 * 统一处理Redis模块抛出的异常，根据不同的错误码进行不同的日志记录和处理
 *
 * @see RedisResultCode
 * @see RedisException
 */
@Slf4j
@Component
public class RedisExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof RedisException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        RedisException ex = (RedisException) throwable;
        int code = ex.getCode();

        // 根据不同的错误码进行不同的处理
        if (code >= RedisResultCode.REDIS_ERROR.getCode() && code < RedisResultCode.CACHE_MISS.getCode()) {
            // 900-900: Redis通用错误
            log.error("Redis通用错误: code={}, message={}", code, ex.getMessage(), ex);
        } else if (code >= RedisResultCode.CACHE_MISS.getCode() && code < RedisResultCode.LOCK_ACQUIRE_FAILED.getCode()) {
            // 901-909: 缓存操作相关错误
            handleCacheError(code, ex);
        } else if (code >= RedisResultCode.LOCK_ACQUIRE_FAILED.getCode() && code < RedisResultCode.RATE_LIMIT_EXCEEDED.getCode()) {
            // 910-919: 分布式锁相关错误
            handleLockError(code, ex);
        } else if (code >= RedisResultCode.RATE_LIMIT_EXCEEDED.getCode() && code < RedisResultCode.CONNECTION_ERROR.getCode()) {
            // 920-929: 限流相关错误
            handleRateLimitError(code, ex);
        } else if (code >= RedisResultCode.CONNECTION_ERROR.getCode() && code < RedisResultCode.SERIALIZATION_ERROR.getCode()) {
            // 930-939: 连接相关错误
            handleConnectionError(code, ex);
        } else if (code >= RedisResultCode.SERIALIZATION_ERROR.getCode() && code < RedisResultCode.COMMAND_EXECUTION_ERROR.getCode()) {
            // 940-949: 序列化相关错误
            handleSerializationError(code, ex);
        } else {
            // 950+: 其他错误
            log.error("Redis其他错误: code={}, message={}", code, ex.getMessage(), ex);
        }

        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理缓存相关错误
     */
    private void handleCacheError(int code, RedisException ex) {
        if (code == RedisResultCode.CACHE_MISS.getCode()) {
            log.debug("Redis缓存未命中: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_GET_FAILED.getCode()) {
            log.error("Redis缓存获取失败: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CACHE_SET_FAILED.getCode()) {
            log.error("Redis缓存设置失败: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CACHE_DELETE_FAILED.getCode()) {
            log.error("Redis缓存删除失败: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CACHE_EXPIRE_FAILED.getCode()) {
            log.error("Redis缓存过期设置失败: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CACHE_CLEAR_FAILED.getCode()) {
            log.error("Redis缓存清理失败: {}", ex.getMessage(), ex);
        } else {
            log.error("Redis缓存操作失败: code={}, message={}", code, ex.getMessage(), ex);
        }
    }

    /**
     * 处理分布式锁相关错误
     */
    private void handleLockError(int code, RedisException ex) {
        if (code == RedisResultCode.LOCK_ACQUIRE_FAILED.getCode()) {
            log.warn("Redis锁获取失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_RELEASE_FAILED.getCode()) {
            log.error("Redis锁释放失败: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.LOCK_EXPIRED.getCode()) {
            log.warn("Redis锁已过期: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_NOT_OWNER.getCode()) {
            log.warn("Redis锁非所有者: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_TIMEOUT.getCode()) {
            log.warn("Redis锁获取超时: {}", ex.getMessage());
        } else {
            log.error("Redis锁操作失败: code={}, message={}", code, ex.getMessage(), ex);
        }
    }

    /**
     * 处理限流相关错误
     */
    private void handleRateLimitError(int code, RedisException ex) {
        if (code == RedisResultCode.RATE_LIMIT_EXCEEDED.getCode()) {
            log.warn("Redis限流触发: {}", ex.getMessage());
        } else if (code == RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getCode()) {
            log.error("Redis限流配置错误: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.RATE_LIMIT_RESET_FAILED.getCode()) {
            log.error("Redis限流重置失败: {}", ex.getMessage(), ex);
        } else {
            log.error("Redis限流错误: code={}, message={}", code, ex.getMessage(), ex);
        }
    }

    /**
     * 处理连接相关错误
     */
    private void handleConnectionError(int code, RedisException ex) {
        if (code == RedisResultCode.CONNECTION_ERROR.getCode()) {
            log.error("Redis连接错误: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CONNECTION_TIMEOUT.getCode()) {
            log.error("Redis连接超时: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CONNECTION_CLOSED.getCode()) {
            log.error("Redis连接已关闭: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.CONNECTION_POOL_EXHAUSTED.getCode()) {
            log.error("Redis连接池耗尽: {}", ex.getMessage(), ex);
        } else {
            log.error("Redis连接错误: code={}, message={}", code, ex.getMessage(), ex);
        }
    }

    /**
     * 处理序列化相关错误
     */
    private void handleSerializationError(int code, RedisException ex) {
        if (code == RedisResultCode.SERIALIZATION_ERROR.getCode()) {
            log.error("Redis序列化错误: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.DESERIALIZATION_ERROR.getCode()) {
            log.error("Redis反序列化错误: {}", ex.getMessage(), ex);
        } else if (code == RedisResultCode.SERIALIZER_NOT_FOUND.getCode()) {
            log.error("Redis序列化器未找到: {}", ex.getMessage(), ex);
        } else {
            log.error("Redis序列化错误: code={}, message={}", code, ex.getMessage(), ex);
        }
    }
}
