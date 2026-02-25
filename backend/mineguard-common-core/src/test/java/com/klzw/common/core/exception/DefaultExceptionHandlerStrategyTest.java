package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import com.klzw.common.core.result.ResultCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 默认异常处理策略测试类
 */
public class DefaultExceptionHandlerStrategyTest {

    private final DefaultExceptionHandlerStrategy strategy = new DefaultExceptionHandlerStrategy();

    @Test
    @DisplayName("验证策略支持任何类型的异常")
    public void testSupportAnyException() {
        BusinessException businessException = new BusinessException(400, "业务错误");
        SystemException systemException = new SystemException(500, "系统错误");
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("参数错误");

        assertTrue(strategy.support(businessException));
        assertTrue(strategy.support(systemException));
        assertTrue(strategy.support(illegalArgumentException));
    }

    @Test
    @DisplayName("验证策略能够正确处理 IllegalArgumentException")
    public void testHandleIllegalArgumentException() {
        String errorMessage = "参数错误";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(ResultCode.INTERNAL_ERROR, result.getCode());
        assertTrue(result.getMessage().contains(errorMessage));
    }

    @Test
    @DisplayName("验证策略能够正确处理 NullPointerException")
    public void testHandleNullPointerException() {
        NullPointerException exception = new NullPointerException("空指针异常");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(ResultCode.INTERNAL_ERROR, result.getCode());
        assertTrue(result.getMessage().contains("空指针异常"));
    }

    @Test
    @DisplayName("验证策略能够正确处理 RuntimeException")
    public void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("运行时异常");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(ResultCode.INTERNAL_ERROR, result.getCode());
        assertTrue(result.getMessage().contains("运行时异常"));
    }
}
