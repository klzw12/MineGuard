package com.klzw.common.database.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.common.database.constant.DatabaseResultCode;
import lombok.Getter;

/**
 * 数据库异常类
 * <p>
 * 用于处理数据库操作相关的异常，包括：
 * - 数据库连接异常
 * - SQL执行异常
 * - 事务异常
 * - 数据源异常
 * - 分页异常
 * <p>
 * 错误码范围：1000-1099
 */
@Getter
public class DatabaseException extends BaseException {

    /**
     * 数据库模块标识
     */
    private static final String MODULE = "database";

    public DatabaseException(int code, String message) {
        super(code, message, MODULE);
    }

    public DatabaseException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }

    public DatabaseException(String message) {
        super(1000, message, MODULE);
    }

    public DatabaseException(String message, Throwable cause) {
        super(1000, message, MODULE, cause);
    }

    public DatabaseException(DatabaseResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public DatabaseException(DatabaseResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }
}
