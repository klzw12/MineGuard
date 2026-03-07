package com.klzw.common.web.exception;

import com.klzw.common.core.exception.BaseException;

public class WebException extends BaseException {
    
    private static final int DEFAULT_CODE = 700;

    public WebException(int code, String message) {
        super(code, message);
    }

    public WebException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public WebException(String message) {
        super(DEFAULT_CODE, message);
    }

    public WebException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
