package com.klzw.common.mq.integration;

import com.klzw.common.mq.AbstractMqIntegrationTest;
import com.klzw.common.mq.config.RabbitMqConfig;
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
    private RabbitMqConfig rabbitMqConfig;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - RabbitMqProperties注入")
    void rabbitMqProperties_Injected() {
        assertNotNull(rabbitMqConfig.getRabbitMqProperties());
    }

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - RabbitTemplate配置")
    void rabbitTemplate_Configured() {
        assertNotNull(rabbitTemplate);
    }
}
