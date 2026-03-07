package com.klzw.common.mq.integration;

import com.klzw.common.mq.AbstractMqIntegrationTest;
import com.klzw.common.mq.constant.MqConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MQ常量集成测试")
@Tag("integration")
class MqConstantsIntegrationTest extends AbstractMqIntegrationTest {

    @Test
    @Tag("integration")
    @DisplayName("集成测试 - 验证常量值")
    void constantValues_Correct() {
        assertEquals("default.exchange", MqConstants.DEFAULT_EXCHANGE);
        assertEquals("delay.exchange", MqConstants.DELAY_EXCHANGE);
        assertEquals("default.queue", MqConstants.DEFAULT_QUEUE);
        assertEquals("delay.queue", MqConstants.DELAY_QUEUE);
        assertEquals("dead.letter.queue", MqConstants.DEAD_LETTER_QUEUE);
        assertEquals("default.routing.key", MqConstants.DEFAULT_ROUTING_KEY);
        assertEquals("delay.routing.key", MqConstants.DELAY_ROUTING_KEY);
    }
}
