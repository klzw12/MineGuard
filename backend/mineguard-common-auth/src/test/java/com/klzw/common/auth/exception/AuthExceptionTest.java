package com.klzw.common.auth.exception;

import com.klzw.common.auth.constant.AuthResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证异常单元测试
 */
class AuthExceptionTest {

    @Test
    void constructor_shouldCreateExceptionWithResultCode() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_EXPIRED;

        AuthException exception = new AuthException(resultCode);

        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(resultCode.getMessage(), exception.getMessage());
    }

    @Test
    void constructor_shouldCreateExceptionWithResultCodeAndCause() {
        AuthResultCode resultCode = AuthResultCode.PERMISSION_DENIED;
        Throwable cause = new RuntimeException("permission check failed");

        AuthException exception = new AuthException(resultCode, cause);

        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(resultCode.getMessage(), exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_shouldCreateExceptionWithResultCodeAndCustomMessage() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_INVALID;
        String customMessage = "自定义错误消息";

        AuthException exception = new AuthException(resultCode, customMessage);

        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void constructor_shouldCreateExceptionWithResultCodeMessageAndCause() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_PARSE_ERROR;
        String customMessage = "解析错误";
        Throwable cause = new RuntimeException("parse error");

        AuthException exception = new AuthException(resultCode, customMessage, cause);

        assertEquals(resultCode.getCode(), exception.getCode());
        assertEquals(customMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void getModule_shouldReturnAuthModule() {
        AuthException exception = new AuthException(AuthResultCode.AUTH_ERROR);

        String module = exception.getModule();

        assertEquals("auth", module);
    }

    @Test
    void getCode_shouldReturnCorrectCode() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_EXPIRED;

        AuthException exception = new AuthException(resultCode);

        assertEquals(resultCode.getCode(), exception.getCode());
    }

    @Test
    void getMessage_shouldReturnCorrectMessage() {
        String customMessage = "test message";

        AuthException exception = new AuthException(AuthResultCode.AUTH_ERROR, customMessage);

        assertEquals(customMessage, exception.getMessage());
    }
}