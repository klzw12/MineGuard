package com.klzw.common.mq.config;

import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.mq.properties.RabbitMqProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@ConditionalOnProperty(prefix = "mineguard.mq.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory connectionFactory(RabbitMqProperties properties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(properties.getHost());
        connectionFactory.setPort(properties.getPort());
        connectionFactory.setUsername(properties.getUsername());
        connectionFactory.setPassword(properties.getPassword());
        connectionFactory.setVirtualHost(properties.getVirtualHost());
        
        if (properties.getConnectionTimeout() != null) {
            connectionFactory.setConnectionTimeout(properties.getConnectionTimeout());
        }
        
        if (properties.getChannelCacheSize() != null) {
            connectionFactory.setChannelCacheSize(properties.getChannelCacheSize());
        }
        
        return connectionFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(MqConstants.DEAD_LETTER_EXCHANGE);
    }

    @Bean
    @ConditionalOnMissingBean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(MqConstants.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    @ConditionalOnMissingBean
    public DirectExchange delayExchange() {
        return new DirectExchange(MqConstants.DELAY_EXCHANGE);
    }

    @Bean
    @ConditionalOnMissingBean
    public Queue delayQueue() {
        return QueueBuilder.durable(MqConstants.DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", MqConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(MqConstants.DELAY_ROUTING_KEY);
    }
}
