package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 业务异常处理策略测试类
 */
public class BusinessExceptionHandlerStrategyTest {

    private final BusinessExceptionHandlerStrategy strategy = new BusinessExceptionHandlerStrategy();

    @Test
    @DisplayName("验证策略支持业务异常")
    public void testSupportBusinessException() {
        BusinessException exception = new BusinessException(400, "业务错误");
        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略不支持系统异常")
    public void testNotSupportSystemException() {
        SystemException exception = new SystemException(500, "系统错误");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略不支持其他类型的异常")
    public void testNotSupportOtherException() {
        IllegalArgumentException exception = new IllegalArgumentException("参数错误");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("验证策略能够正确处理业务异常")
    public void testHandleBusinessException() {
        int errorCode = 400;
        String errorMessage = "业务错误";
        BusinessException exception = new BusinessException(errorCode, errorMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
    }
}
