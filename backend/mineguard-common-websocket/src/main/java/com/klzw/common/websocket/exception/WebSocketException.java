package com.klzw.common.websocket.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.common.websocket.constant.WebSocketResultCode;
import lombok.Getter;

@Getter
public class WebSocketException extends BaseException {
    private static final String MODULE = "websocket";

    public WebSocketException(WebSocketResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public WebSocketException(WebSocketResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    public WebSocketException(WebSocketResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    public WebSocketException(WebSocketResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, MODULE, cause);
    }

    public WebSocketException(int code, String message) {
        super(code, message, MODULE);
    }

    public WebSocketException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }
}
