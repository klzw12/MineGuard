package com.klzw.common.web.handler;

import com.klzw.common.core.result.Result;
import com.klzw.common.web.constant.WebResultCode;
import com.klzw.common.web.exception.WebException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebExceptionHandlerStrategy 单元测试")
public class WebExceptionHandlerStrategyTest {

    private WebExceptionHandlerStrategy strategy;

    @BeforeEach
    public void setUp() {
        strategy = new WebExceptionHandlerStrategy();
    }

    @Test
    @DisplayName("测试支持WebException")
    public void testSupportWebException() {
        WebException exception = new WebException(WebResultCode.PARAM_MISSING.getCode(), "参数缺失");
        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("测试不支持其他异常")
    public void testNotSupportOtherException() {
        Exception exception = new Exception("普通异常");
        assertFalse(strategy.support(exception));
    }

    @Test
    @DisplayName("测试处理WebException")
    public void testHandleWebException() {
        WebException exception = new WebException(WebResultCode.PARAM_MISSING.getCode(), "参数缺失");
        
        Result<?> result = strategy.handle(exception);
        
        assertNotNull(result);
        assertEquals(WebResultCode.PARAM_MISSING.getCode(), result.getCode());
        assertEquals("参数缺失", result.getMessage());
    }

    @Test
    @DisplayName("测试处理文件上传异常")
    public void testHandleFileUploadException() {
        WebException exception = new WebException(WebResultCode.FILE_UPLOAD_ERROR.getCode(), "文件上传失败");
        
        Result<?> result = strategy.handle(exception);
        
        assertNotNull(result);
        assertEquals(WebResultCode.FILE_UPLOAD_ERROR.getCode(), result.getCode());
        assertEquals("文件上传失败", result.getMessage());
    }
}
