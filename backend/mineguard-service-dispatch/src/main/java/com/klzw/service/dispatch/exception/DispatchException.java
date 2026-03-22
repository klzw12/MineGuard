package com.klzw.service.dispatch.exception;

import com.klzw.common.core.exception.BaseException;

/**
 * 调度模块自定义异常类
 */
public class DispatchException extends BaseException {
    
    public DispatchException(DispatchResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage());
    }
    
    public DispatchException(DispatchResultCode resultCode, String message) {
        super(resultCode.getCode(), message);
    }
    
    public DispatchException(Integer code, String message) {
        super(code, message);
    }
}
