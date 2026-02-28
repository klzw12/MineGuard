package com.klzw.common.redis.exception;

import com.klzw.common.core.exception.BaseException;
import lombok.Getter;

/**
 * Redis 异常类
 */
@Getter
public class RedisException extends BaseException {

    /**
     * Redis模块标识
     */
    private static final String MODULE = "redis";

    // 错误码定义
    public static final int REDIS_CONNECTION_ERROR = 900; // 连接错误
    public static final int REDIS_SERIALIZATION_ERROR = 901; // 序列化错误
    public static final int REDIS_DESERIALIZATION_ERROR = 902; // 反序列化错误
    public static final int REDIS_LOCK_ACQUIRE_ERROR = 903; // 锁获取错误
    public static final int REDIS_LOCK_RELEASE_ERROR = 904; // 锁释放错误
    public static final int REDIS_RATE_LIMIT_ERROR = 905; // 限流错误
    public static final int REDIS_COMMAND_ERROR = 906; // 命令执行错误
    public static final int REDIS_TIMEOUT_ERROR = 907; // 超时错误

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误消息
     */
    public RedisException(int code, String message) {
        super(code, message, MODULE);
    }

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public RedisException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }

    /**
     * 构造方法
     * @param message 错误消息
     */
    public RedisException(String message) {
        super(REDIS_COMMAND_ERROR, message, MODULE);
    }

    /**
     * 构造方法
     * @param message 错误消息
     * @param cause 异常原因
     */
    public RedisException(String message, Throwable cause) {
        super(REDIS_COMMAND_ERROR, message, MODULE, cause);
    }

    /**
     * 创建连接错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException connectionError(String message) {
        return new RedisException(REDIS_CONNECTION_ERROR, message);
    }

    /**
     * 创建连接错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException connectionError(String message, Throwable cause) {
        return new RedisException(REDIS_CONNECTION_ERROR, message, cause);
    }

    /**
     * 创建序列化错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException serializationError(String message) {
        return new RedisException(REDIS_SERIALIZATION_ERROR, message);
    }

    /**
     * 创建序列化错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException serializationError(String message, Throwable cause) {
        return new RedisException(REDIS_SERIALIZATION_ERROR, message, cause);
    }

    /**
     * 创建反序列化错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException deserializationError(String message) {
        return new RedisException(REDIS_DESERIALIZATION_ERROR, message);
    }

    /**
     * 创建反序列化错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException deserializationError(String message, Throwable cause) {
        return new RedisException(REDIS_DESERIALIZATION_ERROR, message, cause);
    }

    /**
     * 创建锁获取错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException lockAcquireError(String message) {
        return new RedisException(REDIS_LOCK_ACQUIRE_ERROR, message);
    }

    /**
     * 创建锁获取错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException lockAcquireError(String message, Throwable cause) {
        return new RedisException(REDIS_LOCK_ACQUIRE_ERROR, message, cause);
    }

    /**
     * 创建锁释放错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException lockReleaseError(String message) {
        return new RedisException(REDIS_LOCK_RELEASE_ERROR, message);
    }

    /**
     * 创建锁释放错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException lockReleaseError(String message, Throwable cause) {
        return new RedisException(REDIS_LOCK_RELEASE_ERROR, message, cause);
    }

    /**
     * 创建限流错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException rateLimitError(String message) {
        return new RedisException(REDIS_RATE_LIMIT_ERROR, message);
    }

    /**
     * 创建限流错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException rateLimitError(String message, Throwable cause) {
        return new RedisException(REDIS_RATE_LIMIT_ERROR, message, cause);
    }

    /**
     * 创建命令执行错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException commandError(String message) {
        return new RedisException(REDIS_COMMAND_ERROR, message);
    }

    /**
     * 创建命令执行错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException commandError(String message, Throwable cause) {
        return new RedisException(REDIS_COMMAND_ERROR, message, cause);
    }

    /**
     * 创建超时错误异常
     * @param message 错误消息
     * @return RedisException
     */
    public static RedisException timeoutError(String message) {
        return new RedisException(REDIS_TIMEOUT_ERROR, message);
    }

    /**
     * 创建超时错误异常
     * @param message 错误消息
     * @param cause 异常原因
     * @return RedisException
     */
    public static RedisException timeoutError(String message, Throwable cause) {
        return new RedisException(REDIS_TIMEOUT_ERROR, message, cause);
    }
}