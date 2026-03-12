package com.klzw.common.redis.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        if (code >= RedisResultCode.REDIS_ERROR.getCode() && code < RedisResultCode.CACHE_MISS.getCode()) {
            log.error("Redis通用错误: code={}, message={}", code, ex.getMessage());
        } else if (code >= RedisResultCode.CACHE_MISS.getCode() && code < RedisResultCode.LOCK_ACQUIRE_FAILED.getCode()) {
            handleCacheError(code, ex);
        } else if (code >= RedisResultCode.LOCK_ACQUIRE_FAILED.getCode() && code < RedisResultCode.RATE_LIMIT_EXCEEDED.getCode()) {
            handleLockError(code, ex);
        } else if (code >= RedisResultCode.RATE_LIMIT_EXCEEDED.getCode() && code < RedisResultCode.CONNECTION_ERROR.getCode()) {
            handleRateLimitError(code, ex);
        } else if (code >= RedisResultCode.CONNECTION_ERROR.getCode() && code < RedisResultCode.SERIALIZATION_ERROR.getCode()) {
            handleConnectionError(code, ex);
        } else if (code >= RedisResultCode.SERIALIZATION_ERROR.getCode() && code < RedisResultCode.COMMAND_EXECUTION_ERROR.getCode()) {
            handleSerializationError(code, ex);
        } else {
            log.error("Redis其他错误: code={}, message={}", code, ex.getMessage());
        }

        return Result.fail(ex.getCode(), ex.getMessage());
    }

    private void handleCacheError(int code, RedisException ex) {
        if (code == RedisResultCode.CACHE_MISS.getCode()) {
            log.debug("Redis缓存未命中: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_GET_FAILED.getCode()) {
            log.error("Redis缓存获取失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_SET_FAILED.getCode()) {
            log.error("Redis缓存设置失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_DELETE_FAILED.getCode()) {
            log.error("Redis缓存删除失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_EXPIRE_FAILED.getCode()) {
            log.error("Redis缓存过期设置失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.CACHE_CLEAR_FAILED.getCode()) {
            log.error("Redis缓存清理失败: {}", ex.getMessage());
        } else {
            log.error("Redis缓存操作失败: code={}, message={}", code, ex.getMessage());
        }
    }

    private void handleLockError(int code, RedisException ex) {
        if (code == RedisResultCode.LOCK_ACQUIRE_FAILED.getCode()) {
            log.warn("Redis锁获取失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_RELEASE_FAILED.getCode()) {
            log.error("Redis锁释放失败: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_EXPIRED.getCode()) {
            log.warn("Redis锁已过期: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_NOT_OWNER.getCode()) {
            log.warn("Redis锁非所有者: {}", ex.getMessage());
        } else if (code == RedisResultCode.LOCK_TIMEOUT.getCode()) {
            log.warn("Redis锁获取超时: {}", ex.getMessage());
        } else {
            log.error("Redis锁操作失败: code={}, message={}", code, ex.getMessage());
        }
    }

    private void handleRateLimitError(int code, RedisException ex) {
        if (code == RedisResultCode.RATE_LIMIT_EXCEEDED.getCode()) {
            log.warn("Redis限流触发: {}", ex.getMessage());
        } else if (code == RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getCode()) {
            log.error("Redis限流配置错误: {}", ex.getMessage());
        } else if (code == RedisResultCode.RATE_LIMIT_RESET_FAILED.getCode()) {
            log.error("Redis限流重置失败: {}", ex.getMessage());
        } else {
            log.error("Redis限流错误: code={}, message={}", code, ex.getMessage());
        }
    }

    private void handleConnectionError(int code, RedisException ex) {
        if (code == RedisResultCode.CONNECTION_ERROR.getCode()) {
            log.error("Redis连接错误: {}", ex.getMessage());
        } else if (code == RedisResultCode.CONNECTION_TIMEOUT.getCode()) {
            log.error("Redis连接超时: {}", ex.getMessage());
        } else if (code == RedisResultCode.CONNECTION_CLOSED.getCode()) {
            log.error("Redis连接已关闭: {}", ex.getMessage());
        } else if (code == RedisResultCode.CONNECTION_POOL_EXHAUSTED.getCode()) {
            log.error("Redis连接池耗尽: {}", ex.getMessage());
        } else {
            log.error("Redis连接错误: code={}, message={}", code, ex.getMessage());
        }
    }

    private void handleSerializationError(int code, RedisException ex) {
        if (code == RedisResultCode.SERIALIZATION_ERROR.getCode()) {
            log.error("Redis序列化错误: {}", ex.getMessage());
        } else if (code == RedisResultCode.DESERIALIZATION_ERROR.getCode()) {
            log.error("Redis反序列化错误: {}", ex.getMessage());
        } else if (code == RedisResultCode.SERIALIZER_NOT_FOUND.getCode()) {
            log.error("Redis序列化器未找到: {}", ex.getMessage());
        } else {
            log.error("Redis序列化错误: code={}, message={}", code, ex.getMessage());
        }
    }
}
