package com.klzw.common.mq.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MqException 单元测试")
class MqExceptionTest {

    @Test
    @DisplayName("创建MqException - 仅错误码和消息")
    void create_WithCodeAndMessage() {
        int code = 1100;
        String message = "消息队列操作失败";

        MqException exception = new MqException(code, message);

        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("创建MqException - 带原因的异常")
    void create_WithCodeMessageAndCause() {
        int code = 1110;
        String message = "消息发送失败";
        Throwable cause = new RuntimeException("Connection failed");

        MqException exception = new MqException(code, message, cause);

        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("验证不同的错误码")
    void verifyDifferentErrorCodes() {
        testExceptionCreation(1100, "通用错误");
        testExceptionCreation(1110, "生产者错误");
        testExceptionCreation(1120, "消费者错误");
        testExceptionCreation(1130, "队列错误");
        testExceptionCreation(1140, "交换机错误");
    }

    private void testExceptionCreation(int code, String message) {
        MqException exception = new MqException(code, message);
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
    }
}
