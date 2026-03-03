package com.klzw.common.mongodb.exception;

import com.klzw.common.core.exception.BaseException;
import lombok.Getter;

/**
 * MongoDB异常类
 * <p>
 * 用于处理MongoDB操作相关的异常，包括：
 * - MongoDB连接异常
 * - 集合操作异常
 * - 文档操作异常
 * - 索引操作异常
 * - 聚合操作异常
 * - 地理空间操作异常
 * <p>
 * 错误码范围：1300-1399
 */
@Getter
public class MongoDbException extends BaseException {

    /**
     * MongoDB模块标识
     */
    private static final String MODULE = "mongodb";

    public MongoDbException(int code, String message) {
        super(code, message, MODULE);
    }

    public MongoDbException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }

    public MongoDbException(String message) {
        super(1300, message, MODULE);
    }

    public MongoDbException(String message, Throwable cause) {
        super(1300, message, MODULE, cause);
    }
}