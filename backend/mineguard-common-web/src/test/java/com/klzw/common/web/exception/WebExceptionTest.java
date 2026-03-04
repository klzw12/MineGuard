package com.klzw.common.web.exception;

import com.klzw.common.web.constant.WebResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebException 单元测试")
public class WebExceptionTest {

    @Test
    @DisplayName("测试创建WebException带错误码和消息")
    public void testCreateWebExceptionWithCodeAndMessage() {
        WebException exception = new WebException(700, "参数缺失");
        
        assertEquals(700, exception.getCode());
        assertEquals("参数缺失", exception.getMessage());
    }

    @Test
    @DisplayName("测试创建WebException带错误码、消息和原因")
    public void testCreateWebExceptionWithCodeMessageAndCause() {
        Throwable cause = new RuntimeException("原始异常");
        WebException exception = new WebException(701, "参数格式错误", cause);
        
        assertEquals(701, exception.getCode());
        assertEquals("参数格式错误", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试使用WebResultCode创建异常")
    public void testCreateWebExceptionWithResultCode() {
        WebException exception = new WebException(
            WebResultCode.PARAM_MISSING.getCode(), 
            WebResultCode.PARAM_MISSING.getMessage()
        );
        
        assertEquals(701, exception.getCode());
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("测试异常继承关系")
    public void testExceptionInheritance() {
        WebException exception = new WebException(700, "测试异常");
        
        assertTrue(exception instanceof RuntimeException);
    }
}
