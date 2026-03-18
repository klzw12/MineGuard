package com.klzw.service.warning.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.warning.constant.WarningResultCode;
import lombok.Getter;

@Getter
public class WarningException extends BaseException {

    private static final String MODULE = "warning";

    public WarningException(int code, String message) {
        super(code, message, MODULE);
    }

    public WarningException(WarningResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public WarningException(WarningResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    public WarningException(WarningResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }
}
