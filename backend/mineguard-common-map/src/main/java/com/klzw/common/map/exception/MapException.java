package com.klzw.common.map.exception;

import com.klzw.common.core.exception.BaseException;

public class MapException extends BaseException {
    
    private static final int DEFAULT_CODE = 1600;

    public MapException(int code, String message) {
        super(code, message);
    }

    public MapException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public MapException(String message) {
        super(DEFAULT_CODE, message);
    }

    public MapException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
