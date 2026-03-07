package com.klzw.common.mq.integration;

import com.klzw.common.mq.AbstractMqIntegrationTest;
import com.klzw.common.mq.producer.IMessageProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("消息队列集成测试")
@Tag("integration")
class MessageQueueIntegrationTest extends AbstractMqIntegrationTest {

    @Autowired
    private IMessageProducer messageProducer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private String testExchange;
    private String testRoutingKey;
    private String testQueue;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString();
        testExchange = "test.exchange." + uniqueId;
        testRoutingKey = "test.routing." + uniqueId;
        testQueue = "test.queue." + uniqueId;

        DirectExchange exchange = new DirectExchange(testExchange, true, false);
        Queue queue = QueueBuilder.durable(testQueue).build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(testRoutingKey);

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.deleteQueue(testQueue);
        rabbitAdmin.deleteExchange(testExchange);
    }

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - 发送和接收普通消息")
    void sendAndReceiveMessage_Success() throws InterruptedException {
        String testMessage = "Test message " + System.currentTimeMillis();

        messageProducer.sendMessage(testExchange, testRoutingKey, testMessage);

        TimeUnit.SECONDS.sleep(1);

        Object receivedMessage = rabbitTemplate.receiveAndConvert(testQueue);
        assertNotNull(receivedMessage);
        assertEquals(testMessage, receivedMessage);
    }

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - 发送批量消息")
    void sendBatchMessages_Success() throws InterruptedException {
        List<Object> messages = Arrays.asList(
            "Batch message 1",
            "Batch message 2",
            "Batch message 3"
        );

        messageProducer.sendBatchMessage(testExchange, testRoutingKey, messages);

        TimeUnit.SECONDS.sleep(1);

        int receivedCount = 0;
        for (int i = 0; i < messages.size(); i++) {
            Object message = rabbitTemplate.receiveAndConvert(testQueue);
            if (message != null) {
                receivedCount++;
            }
        }

        assertEquals(messages.size(), receivedCount);
    }
}
