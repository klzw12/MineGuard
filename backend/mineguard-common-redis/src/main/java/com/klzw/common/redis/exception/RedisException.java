package com.klzw.common.redis.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.common.redis.constant.RedisResultCode;
import lombok.Getter;

/**
 * Redis 异常类
 * <p>
 * 用于处理Redis操作相关的异常，包括：
 * - Redis连接异常
 * - 缓存操作异常
 * - 分布式锁异常
 * - 限流异常
 * - 序列化异常
 * <p>
 * 错误码范围：900-999（统一使用RedisResultCode定义）
 *
 * @see RedisResultCode
 */
@Getter
public class RedisException extends BaseException {

    /**
     * Redis模块标识
     */
    private static final String MODULE = "redis";

    /**
     * 构造方法 - 使用RedisResultCode
     *
     * @param resultCode Redis错误码枚举
     */
    public RedisException(RedisResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    /**
     * 构造方法 - 使用RedisResultCode和自定义消息
     *
     * @param resultCode Redis错误码枚举
     * @param message    自定义错误消息
     */
    public RedisException(RedisResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    /**
     * 构造方法 - 使用RedisResultCode和异常原因
     *
     * @param resultCode Redis错误码枚举
     * @param cause      异常原因
     */
    public RedisException(RedisResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    /**
     * 构造方法 - 使用RedisResultCode、自定义消息和异常原因
     *
     * @param resultCode Redis错误码枚举
     * @param message    自定义错误消息
     * @param cause      异常原因
     */
    public RedisException(RedisResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, MODULE, cause);
    }

    /**
     * 构造方法 - 使用错误码和消息（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public RedisException(int code, String message) {
        super(code, message, MODULE);
    }

    /**
     * 构造方法 - 使用错误码、消息和异常原因（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   异常原因
     */
    public RedisException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }
}
