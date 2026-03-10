package com.klzw.common.mq.integration;

import com.klzw.common.mq.AbstractMqIntegrationTest;
import com.klzw.common.mq.properties.RabbitMqProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RabbitMQ配置集成测试")
@Tag("integration")
class RabbitMqConfigIntegrationTest extends AbstractMqIntegrationTest {

    @Autowired
    private RabbitMqProperties rabbitMqProperties;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - RabbitMqProperties注入")
    void rabbitMqProperties_Injected() {
        assertNotNull(rabbitMqProperties);
        assertEquals("192.168.110.128", rabbitMqProperties.getHost());
        assertEquals(5673, rabbitMqProperties.getPort());
        assertEquals("admin", rabbitMqProperties.getUsername());
        assertEquals("rabbitmqadmin", rabbitMqProperties.getPassword());
    }

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - RabbitTemplate配置")
    void rabbitTemplate_Configured() {
        assertNotNull(rabbitTemplate);
    }
}
