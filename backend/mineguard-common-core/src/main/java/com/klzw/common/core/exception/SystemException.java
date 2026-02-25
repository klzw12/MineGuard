package com.klzw.common.core.exception;

public class SystemException extends BaseException {
    public SystemException(int code, String message) {
        super(code, message);
    }

    public SystemException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
