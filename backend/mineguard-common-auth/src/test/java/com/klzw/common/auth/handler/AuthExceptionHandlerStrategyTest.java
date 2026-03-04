package com.klzw.common.auth.handler;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证异常处理策略单元测试
 */
class AuthExceptionHandlerStrategyTest {

    private AuthExceptionHandlerStrategy authExceptionHandlerStrategy;

    @BeforeEach
    void setUp() {
        authExceptionHandlerStrategy = new AuthExceptionHandlerStrategy();
    }

    @Test
    void support_shouldReturnTrue_whenAuthException() {
        AuthException exception = new AuthException(AuthResultCode.AUTH_ERROR);

        boolean result = authExceptionHandlerStrategy.support(exception);

        assertTrue(result);
    }

    @Test
    void support_shouldReturnFalse_whenOtherException() {
        RuntimeException exception = new RuntimeException("test");

        boolean result = authExceptionHandlerStrategy.support(exception);

        assertFalse(result);
    }

    @Test
    void handle_shouldReturnFailResult() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_EXPIRED;
        AuthException exception = new AuthException(resultCode);

        Result<?> result = authExceptionHandlerStrategy.handle(exception);

        assertNotNull(result);
        assertEquals(resultCode.getCode(), result.getCode());
        assertEquals(resultCode.getMessage(), result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void handle_shouldHandleDifferentAuthExceptions() {
        // 测试不同的认证异常
        AuthResultCode[] resultCodes = {
            AuthResultCode.TOKEN_INVALID,
            AuthResultCode.TOKEN_MISSING,
            AuthResultCode.PERMISSION_DENIED,
            AuthResultCode.ROLE_DENIED,
            AuthResultCode.USER_NOT_FOUND,
            AuthResultCode.PASSWORD_ERROR,
            AuthResultCode.ACCOUNT_LOCKED
        };

        for (AuthResultCode resultCode : resultCodes) {
            AuthException exception = new AuthException(resultCode);
            Result<?> result = authExceptionHandlerStrategy.handle(exception);

            assertNotNull(result);
            assertEquals(resultCode.getCode(), result.getCode());
            assertEquals(resultCode.getMessage(), result.getMessage());
            assertNull(result.getData());
        }
    }

    @Test
    void handle_shouldHandleExceptionWithCustomMessage() {
        AuthResultCode resultCode = AuthResultCode.TOKEN_PARSE_ERROR;
        String customMessage = "解析错误详情";
        AuthException exception = new AuthException(resultCode, customMessage);

        Result<?> result = authExceptionHandlerStrategy.handle(exception);

        assertNotNull(result);
        assertEquals(resultCode.getCode(), result.getCode());
        assertEquals(customMessage, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void support_shouldReturnFalse_whenNull() {
        boolean result = authExceptionHandlerStrategy.support(null);

        assertFalse(result);
    }
}