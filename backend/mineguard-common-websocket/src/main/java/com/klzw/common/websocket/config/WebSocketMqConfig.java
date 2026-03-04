package com.klzw.common.websocket.config;

import com.klzw.common.mq.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketMqConfig {

    @Value("${websocket.offline-message.enabled:true}")
    private boolean offlineMessageEnabled;

    @Bean
    public TopicExchange offlineMessageExchange() {
        return new TopicExchange(MqConstants.OFFLINE_MESSAGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue offlineMessageQueue() {
        return QueueBuilder.durable(MqConstants.OFFLINE_MESSAGE_QUEUE_PREFIX + "queue")
                .withArgument("x-dead-letter-exchange", MqConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    @Bean
    public Binding offlineMessageBinding() {
        return BindingBuilder.bind(offlineMessageQueue())
                .to(offlineMessageExchange())
                .with(MqConstants.OFFLINE_MESSAGE_ROUTING_KEY_PREFIX + "*");
    }

    public boolean isOfflineMessageEnabled() {
        return offlineMessageEnabled;
    }
}
