package com.klzw.common.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * RabbitMQ 配置类
 */
@Configuration
@EnableTransactionManagement
public class RabbitMqConfig {



    private final RabbitMqProperties rabbitMqProperties;

    // 构造器注入替代 @Autowired
    public RabbitMqConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * 事务管理器
     */
    @Bean
    public RabbitTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    /**
     * 监听容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    /**
     * 死信交换机
     */
    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange("dead.letter.exchange");
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dead.letter.queue").build();
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange("delay.exchange");
    }

    /**
     * 延迟队列
     */
    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable("delay.queue")
                .withArgument("x-dead-letter-exchange", "dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("delay.routing.key");
    }

    /**
     * 创建队列时添加死信配置
     */
    public Queue createQueueWithDeadLetter(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    /**
     * 创建延迟队列（带TTL）
     */
    public Queue createDelayQueueWithTTL(String queueName, long ttl) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "")
                .withArgument("x-message-ttl", ttl)
                .build();
    }

    public RabbitMqProperties getRabbitMqProperties() {
        return rabbitMqProperties;
    }
}
