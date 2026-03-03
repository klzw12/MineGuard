package com.klzw.common.mq.exception;

import com.klzw.common.core.result.Result;
import com.klzw.common.mq.constant.MqResultCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MqExceptionHandlerStrategy 单元测试")
class MqExceptionHandlerStrategyTest {

    private MqExceptionHandlerStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MqExceptionHandlerStrategy();
    }

    @Test
    @DisplayName("支持MqException异常")
    void support_MqException_ReturnsTrue() {
        MqException exception = new MqException(1100, "Test exception");

        boolean result = strategy.support(exception);

        assertTrue(result);
    }

    @Test
    @DisplayName("不支持其他异常类型")
    void support_OtherException_ReturnsFalse() {
        RuntimeException exception = new RuntimeException("Test exception");

        boolean result = strategy.support(exception);

        assertFalse(result);
    }

    @Test
    @DisplayName("不支持null异常")
    void support_NullException_ReturnsFalse() {
        boolean result = strategy.support(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("处理MqException - 返回正确的错误结果")
    void handle_MqException_ReturnsFailResult() {
        int errorCode = 1110;
        String errorMessage = "消息发送失败";
        MqException exception = new MqException(errorCode, errorMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    @DisplayName("处理MqException - 带原因的异常")
    void handle_MqExceptionWithCause_ReturnsFailResult() {
        int errorCode = 1100;
        String errorMessage = "消息队列操作失败";
        Throwable cause = new RuntimeException("Connection failed");
        MqException exception = new MqException(errorCode, errorMessage, cause);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    @DisplayName("处理不同错误码的MqException")
    void handle_DifferentErrorCodes_ReturnsCorrectResult() {
        testErrorCode(MqResultCode.MQ_ERROR);
        testErrorCode(MqResultCode.PRODUCER_SEND_ERROR);
        testErrorCode(MqResultCode.PRODUCER_TRANSACTION_ERROR);
        testErrorCode(MqResultCode.CONSUMER_PROCESS_ERROR);
        testErrorCode(MqResultCode.CONSUMER_RETRY_EXCEEDED);
    }

    private void testErrorCode(MqResultCode resultCode) {
        MqException exception = new MqException(resultCode.getCode(), resultCode.getMessage());

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(resultCode.getCode(), result.getCode());
        assertEquals(resultCode.getMessage(), result.getMessage());
    }
}
