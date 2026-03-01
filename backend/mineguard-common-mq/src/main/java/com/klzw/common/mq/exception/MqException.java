package com.klzw.common.mq.exception;

import com.klzw.common.core.exception.BaseException;

/**
 * 消息队列相关异常
 * 错误码范围：1100-1199
 */
public class MqException extends BaseException {
    
    public MqException(int code, String message) {
        super(code, message);
    }
    
    public MqException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
