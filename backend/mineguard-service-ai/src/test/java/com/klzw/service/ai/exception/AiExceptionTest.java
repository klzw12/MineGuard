package com.klzw.service.ai.exception;

import com.klzw.service.ai.constant.AiResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiExceptionTest {

    @Test
    void testConstructor_WithCodeAndMessage() {
        AiException exception = new AiException(2600, "AI模型不存在");

        assertEquals(2600, exception.getCode());
        assertEquals("AI模型不存在", exception.getMessage());
        assertEquals("ai", exception.getModule());
    }

    @Test
    void testConstructor_WithResultCode() {
        AiException exception = new AiException(AiResultCode.AI_ANALYSIS_FAILED);

        assertEquals(2610, exception.getCode());
        assertEquals("AI分析失败", exception.getMessage());
        assertEquals("ai", exception.getModule());
    }

    @Test
    void testConstructor_WithResultCodeAndCustomMessage() {
        AiException exception = new AiException(AiResultCode.AI_PROVIDER_NOT_AVAILABLE, "DeepSeek服务不可用");

        assertEquals(2640, exception.getCode());
        assertEquals("DeepSeek服务不可用", exception.getMessage());
        assertEquals("ai", exception.getModule());
    }

    @Test
    void testException_IsRuntimeException() {
        AiException exception = new AiException(AiResultCode.AI_REQUEST_TIMEOUT);

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testException_CanBeCaughtAsBaseException() {
        AiException exception = new AiException(AiResultCode.AI_API_KEY_INVALID);

        assertTrue(exception instanceof com.klzw.common.core.exception.BaseException);
        assertEquals(2641, exception.getCode());
    }
}
