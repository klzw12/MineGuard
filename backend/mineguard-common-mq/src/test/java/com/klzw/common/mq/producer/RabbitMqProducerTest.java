package com.klzw.common.mq.producer;

import com.klzw.common.mq.exception.MqException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMqProducer 单元测试")
public class RabbitMqProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMqProducer producer;

    private static final String EXCHANGE = "test.exchange";
    private static final String ROUTING_KEY = "test.routing.key";
    private static final Object TEST_MESSAGE = "test message";

    @BeforeEach
    void setUp() {
        producer = new RabbitMqProducer(rabbitTemplate);
    }

    @Test
    @DisplayName("发送普通消息 - 成功场景")
    void sendMessage_Success() {
        producer.sendMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE);

        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            eq(TEST_MESSAGE),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("发送普通消息 - AmqpException异常场景")
    void sendMessage_AmqpException_ThrowsMqException() {
        doThrow(new AmqpException("Connection failed"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE)
        );

        assertEquals(1110, exception.getCode());
        assertEquals("消息发送失败", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("发送普通消息 - 其他异常场景")
    void sendMessage_OtherException_ThrowsMqException() {
        doThrow(new RuntimeException("Unexpected error"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE)
        );

        assertEquals(1100, exception.getCode());
        assertEquals("消息队列操作失败", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("发送延迟消息 - 成功场景")
    void sendDelayMessage_Success() {
        long delayMillis = 5000L;

        producer.sendDelayMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE, delayMillis);

        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            eq(TEST_MESSAGE),
            any(),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("发送延迟消息 - AmqpException异常场景")
    void sendDelayMessage_AmqpException_ThrowsMqException() {
        long delayMillis = 5000L;

        doThrow(new AmqpException("Connection failed"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendDelayMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE, delayMillis)
        );

        assertEquals(1110, exception.getCode());
        assertEquals("延迟消息发送失败", exception.getMessage());
    }

    @Test
    @DisplayName("发送延迟消息 - 其他异常场景")
    void sendDelayMessage_OtherException_ThrowsMqException() {
        long delayMillis = 5000L;

        doThrow(new RuntimeException("Unexpected error"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendDelayMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE, delayMillis)
        );

        assertEquals(1100, exception.getCode());
        assertEquals("消息队列操作失败", exception.getMessage());
    }

    @Test
    @DisplayName("发送事务消息 - 成功场景")
    void sendTransactionMessage_Success() {
        boolean result = producer.sendTransactionMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE);

        assertTrue(result);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            eq(TEST_MESSAGE),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("发送事务消息 - AmqpException异常场景")
    void sendTransactionMessage_AmqpException_ThrowsMqException() {
        doThrow(new AmqpException("Transaction failed"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendTransactionMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE)
        );

        assertEquals(1111, exception.getCode());
        assertEquals("事务消息处理失败", exception.getMessage());
    }

    @Test
    @DisplayName("发送事务消息 - 其他异常场景")
    void sendTransactionMessage_OtherException_ThrowsMqException() {
        doThrow(new RuntimeException("Unexpected error"))
            .when(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(CorrelationData.class)
            );

        MqException exception = assertThrows(
            MqException.class,
            () -> producer.sendTransactionMessage(EXCHANGE, ROUTING_KEY, TEST_MESSAGE)
        );

        assertEquals(1100, exception.getCode());
        assertEquals("消息队列操作失败", exception.getMessage());
    }

    @Test
    @DisplayName("批量发送消息 - 成功场景")
    void sendBatchMessage_Success() {
        List<Object> messages = Arrays.asList("msg1", "msg2", "msg3");

        producer.sendBatchMessage(EXCHANGE, ROUTING_KEY, messages);

        verify(rabbitTemplate, times(3)).convertAndSend(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            any(Object.class),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("批量发送消息 - 空列表场景")
    void sendBatchMessage_EmptyList() {
        producer.sendBatchMessage(EXCHANGE, ROUTING_KEY, Collections.emptyList());

        verify(rabbitTemplate, never()).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("批量发送消息 - null列表场景")
    void sendBatchMessage_NullList() {
        producer.sendBatchMessage(EXCHANGE, ROUTING_KEY, null);

        verify(rabbitTemplate, never()).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("批量发送消息 - 部分失败场景")
    void sendBatchMessage_PartialFailure() {
        List<Object> messages = Arrays.asList("msg1", "msg2", "msg3");

        doThrow(new AmqpException("Failed"))
            .when(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq(ROUTING_KEY),
                eq("msg2"),
                any(CorrelationData.class)
            );

        producer.sendBatchMessage(EXCHANGE, ROUTING_KEY, messages);

        verify(rabbitTemplate, times(3)).convertAndSend(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            any(Object.class),
            any(CorrelationData.class)
        );
    }
}
