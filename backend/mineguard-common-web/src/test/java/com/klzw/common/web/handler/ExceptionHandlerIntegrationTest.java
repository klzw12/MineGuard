package com.klzw.common.web.handler;

import com.klzw.common.core.exception.BusinessException;
import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.SystemException;
import com.klzw.common.core.result.Result;
import com.klzw.common.web.TestWebApplication;
import com.klzw.common.web.constant.WebResultCode;
import com.klzw.common.web.exception.WebException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestWebApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("异常处理集成测试")
public class ExceptionHandlerIntegrationTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private WebExceptionHandlerStrategy webExceptionHandlerStrategy;

    @Autowired
    private ExceptionHandlerRegistry exceptionHandlerRegistry;

    @Test
    @DisplayName("测试异常处理器注册表加载")
    public void testExceptionHandlerRegistryLoaded() {
        assertNotNull(exceptionHandlerRegistry);
    }

    @Test
    @DisplayName("测试WebException策略加载")
    public void testWebExceptionStrategyLoaded() {
        assertNotNull(webExceptionHandlerStrategy);
    }

    @Test
    @DisplayName("测试全局异常处理器加载")
    public void testGlobalExceptionHandlerLoaded() {
        assertNotNull(globalExceptionHandler);
    }

    @Test
    @DisplayName("测试处理WebException")
    public void testHandleWebException() {
        WebException exception = new WebException(WebResultCode.PARAM_MISSING.getCode(), "参数缺失");
        
        Result<?> result = globalExceptionHandler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(WebResultCode.PARAM_MISSING.getCode(), result.getCode());
        assertEquals("参数缺失", result.getMessage());
    }

    @Test
    @DisplayName("测试处理BusinessException")
    public void testHandleBusinessException() {
        BusinessException exception = new BusinessException(600, "业务错误");
        
        Result<?> result = globalExceptionHandler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(600, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理SystemException")
    public void testHandleSystemException() {
        SystemException exception = new SystemException(500, "系统错误");
        
        Result<?> result = globalExceptionHandler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
    }

    @Test
    @DisplayName("测试处理普通Exception")
    public void testHandleGeneralException() {
        Exception exception = new Exception("普通错误");
        
        Result<?> result = globalExceptionHandler.handleException(exception);
        
        assertNotNull(result);
        assertEquals(500, result.getCode());
    }
}
