package com.klzw.common.web.handler;

import com.klzw.common.core.exception.BusinessException;
import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.SystemException;
import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("测试处理业务异常")
    public void testHandleBusinessException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ExceptionHandlerRegistry());
        BusinessException exception = new BusinessException(600, "业务错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(600, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理系统异常")
    public void testHandleSystemException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ExceptionHandlerRegistry());
        SystemException exception = new SystemException(500, "系统错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理普通异常")
    public void testHandleGeneralException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ExceptionHandlerRegistry());
        Exception exception = new Exception("普通错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertNotNull(result.getMessage());
    }

    // 自定义策略的注册与顺序，交由 ExceptionHandlerRegistry 的测试覆盖，这里不再重复验证
}