package com.klzw.service.dispatch.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.dispatch.constant.DispatchResultCode;

/**
 * 调度模块异常类
 */
public class DispatchException extends BaseException {

    public DispatchException(DispatchResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage());
    }

    public DispatchException(DispatchResultCode resultCode, String message) {
        super(resultCode.getCode(), message);
    }

    public DispatchException(DispatchResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), cause);
    }

    public DispatchException(DispatchResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, cause);
    }
}
