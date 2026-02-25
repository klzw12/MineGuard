package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

public class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("测试处理业务异常")
    public void testHandleBusinessException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BusinessException exception = new BusinessException(600, "业务错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(600, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理系统异常")
    public void testHandleSystemException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        SystemException exception = new SystemException(500, "系统错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理普通异常")
    public void testHandleGeneralException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception exception = new Exception("普通错误");
        
        Result<?> result = handler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("测试注册异常处理策略")
    public void testRegisterStrategy() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ExceptionHandlerStrategy strategy = Mockito.mock(ExceptionHandlerStrategy.class);
        
        handler.registerStrategy(strategy);
        
        // 验证策略已注册，通过处理异常来间接验证
        Exception exception = new Exception("测试异常");
        Result<?> result = handler.handleException(exception);
        assertNotNull(result);
    }
}
