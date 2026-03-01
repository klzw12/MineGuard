package com.klzw.common.mq.consumer;

import com.klzw.common.mq.exception.MqException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseMessageConsumer 单元测试")
class BaseMessageConsumerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private TestMessageConsumer consumer;

    private static final String EXCHANGE = "test.exchange";
    private static final String ROUTING_KEY = "test.routing.key";

    @BeforeEach
    void setUp() {
        consumer = new TestMessageConsumer();
        consumer.setRabbitTemplate(rabbitTemplate);
    }

    @Test
    @DisplayName("消费消息 - 成功场景")
    void consume_Success() {
        Message message = createTestMessage(null);

        consumer.consume(message);

        assertTrue(consumer.isConsumed());
        verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    }

    @Test
    @DisplayName("消费消息 - 第一次失败后重试成功")
    void consume_FirstFailureThenRetrySuccess() {
        Message message = createTestMessage(null);
        consumer.setFailCount(1);

        consumer.consume(message);

        verify(rabbitTemplate, times(1)).send(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            any(Message.class)
        );
    }

    @Test
    @DisplayName("消费消息 - 达到最大重试次数后发送到死信队列")
    void consume_MaxRetryExceeded_SendToDeadLetterQueue() {
        Message message = createTestMessage(3);
        consumer.setAlwaysFail(true);

        MqException exception = assertThrows(
            MqException.class,
            () -> consumer.consume(message)
        );

        assertEquals(1121, exception.getCode());
        assertEquals("消息重试次数超过限制", exception.getMessage());
        verify(rabbitTemplate, times(1)).send(
            eq("dead.letter.exchange"),
            eq(""),
            any(Message.class)
        );
    }

    @Test
    @DisplayName("消费消息 - 重试发送失败后发送到死信队列")
    void consume_RetrySendFailed_SendToDeadLetterQueue() {
        Message message = createTestMessage(null);
        consumer.setAlwaysFail(true);

        doThrow(new AmqpException("Send failed"))
            .when(rabbitTemplate).send(
                eq(EXCHANGE),
                eq(ROUTING_KEY),
                any(Message.class)
            );

        consumer.consume(message);

        verify(rabbitTemplate, times(1)).send(
            eq("dead.letter.exchange"),
            eq(""),
            any(Message.class)
        );
    }

    @Test
    @DisplayName("消费消息 - AmqpException异常处理")
    void consume_AmqpException_HandledCorrectly() {
        Message message = createTestMessage(null);
        consumer.setAlwaysFail(true);

        consumer.consume(message);

        verify(rabbitTemplate, times(1)).send(
            eq(EXCHANGE),
            eq(ROUTING_KEY),
            any(Message.class)
        );
    }

    @Test
    @DisplayName("消费消息 - 死信队列发送失败")
    void consume_DeadLetterQueueSendFailed_LogError() {
        Message message = createTestMessage(3);
        consumer.setAlwaysFail(true);

        doThrow(new AmqpException("Dead letter queue failed"))
            .when(rabbitTemplate).send(
                eq("dead.letter.exchange"),
                eq(""),
                any(Message.class)
            );

        assertThrows(
            MqException.class,
            () -> consumer.consume(message)
        );

        verify(rabbitTemplate, times(1)).send(
            eq("dead.letter.exchange"),
            eq(""),
            any(Message.class)
        );
    }

    private Message createTestMessage(Integer retryCount) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedExchange(EXCHANGE);
        properties.setReceivedRoutingKey(ROUTING_KEY);
        
        if (retryCount != null) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("retryCount", retryCount);
            properties.setHeaders(headers);
        }
        
        return new Message("test message".getBytes(), properties);
    }

    static class TestMessageConsumer extends BaseMessageConsumer {
        private boolean consumed = false;
        private int failCount = 0;
        private boolean alwaysFail = false;

        public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
            try {
                java.lang.reflect.Field field = BaseMessageConsumer.class.getDeclaredField("rabbitTemplate");
                field.setAccessible(true);
                field.set(this, rabbitTemplate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void doConsume(Message message) throws Exception {
            if (alwaysFail) {
                throw new RuntimeException("Always fail for testing");
            }
            
            if (failCount > 0) {
                failCount--;
                throw new RuntimeException("Simulated failure");
            }
            
            consumed = true;
        }

        public boolean isConsumed() {
            return consumed;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public void setAlwaysFail(boolean alwaysFail) {
            this.alwaysFail = alwaysFail;
        }
    }
}
