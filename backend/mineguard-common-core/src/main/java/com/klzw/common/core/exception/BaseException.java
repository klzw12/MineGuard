package com.klzw.common.core.exception;

import lombok.Getter;

/**
 * 异常基类
 */
@Getter
public class BaseException extends RuntimeException {
    private final int code;
    private final String message;
    private final String module;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.module = this.getClass().getPackage().getName();
    }

    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.module = this.getClass().getPackage().getName();
    }

    public BaseException(int code, String message, String module) {
        super(message);
        this.code = code;
        this.message = message;
        this.module = module;
    }

    public BaseException(int code, String message, String module, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.module = module;
    }
}
