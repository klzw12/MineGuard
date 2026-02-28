package com.klzw.common.redis.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.common.redis.exception.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis 异常处理策略
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
        
        // 根据不同的错误码进行不同的处理
        switch (ex.getCode()) {
            case RedisException.REDIS_CONNECTION_ERROR:
                log.error("Redis连接错误: {}", ex.getMessage(), ex);
                // 可以添加连接重试逻辑或其他处理
                break;
            case RedisException.REDIS_SERIALIZATION_ERROR:
                log.error("Redis序列化错误: {}", ex.getMessage(), ex);
                // 可以添加序列化失败的处理逻辑
                break;
            case RedisException.REDIS_DESERIALIZATION_ERROR:
                log.error("Redis反序列化错误: {}", ex.getMessage(), ex);
                // 可以添加反序列化失败的处理逻辑
                break;
            case RedisException.REDIS_LOCK_ACQUIRE_ERROR:
                log.error("Redis锁获取错误: {}", ex.getMessage(), ex);
                // 可以添加锁获取失败的处理逻辑
                break;
            case RedisException.REDIS_LOCK_RELEASE_ERROR:
                log.error("Redis锁释放错误: {}", ex.getMessage(), ex);
                // 可以添加锁释放失败的处理逻辑
                break;
            case RedisException.REDIS_RATE_LIMIT_ERROR:
                log.warn("Redis限流错误: {}", ex.getMessage());
                // 限流错误通常不需要详细的错误堆栈
                break;
            case RedisException.REDIS_COMMAND_ERROR:
                log.error("Redis命令执行错误: {}", ex.getMessage(), ex);
                // 可以添加命令执行失败的处理逻辑
                break;
            case RedisException.REDIS_TIMEOUT_ERROR:
                log.error("Redis超时错误: {}", ex.getMessage(), ex);
                // 可以添加超时处理逻辑
                break;
            default:
                log.error("Redis未知错误: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        }
        
        return Result.fail(ex.getCode(), ex.getMessage());
    }
}
