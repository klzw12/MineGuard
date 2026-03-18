package com.klzw.service.ai.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.ai.constant.AiResultCode;
import lombok.Getter;

@Getter
public class AiException extends BaseException {

    private static final String MODULE = "ai";

    public AiException(int code, String message) {
        super(code, message, MODULE);
    }

    public AiException(AiResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public AiException(AiResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }
}
