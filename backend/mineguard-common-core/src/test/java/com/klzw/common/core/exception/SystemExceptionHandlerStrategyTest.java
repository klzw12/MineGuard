package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 系统异常处理策略测试类
 */
public class SystemExceptionHandlerStrategyTest {

    private final SystemExceptionHandlerStrategy strategy = new SystemExceptionHandlerStrategy();

    @Test
    @DisplayName("验证策略支持系统异常")
    public void testSupportSystemException() {
        SystemException exception = new SystemException(500, "系统错误");
        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略不支持业务异常")
    public void testNotSupportBusinessException() {
        BusinessException exception = new BusinessException(400, "业务错误");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略不支持其他类型的异常")
    public void testNotSupportOtherException() {
        IllegalArgumentException exception = new IllegalArgumentException("参数错误");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略能够正确处理系统异常")
    public void testHandleSystemException() {
        int errorCode = 500;
        String errorMessage = "系统错误";
        SystemException exception = new SystemException(errorCode, errorMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
    }
}
