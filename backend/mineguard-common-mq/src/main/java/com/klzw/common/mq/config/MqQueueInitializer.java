package com.klzw.common.mq.config;

import com.klzw.common.mq.constant.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列初始化器
 * 在所有单例 Bean 初始化完成后立即声明队列
 * 使用 SmartInitializingSingleton 确保在 @RabbitListener 启动前完成队列声明
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "mineguard.mq.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqQueueInitializer implements SmartInitializingSingleton {

    private final RabbitAdmin rabbitAdmin;

    public MqQueueInitializer(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @Override
    public void afterSingletonsInstantiated() {
        log.info("开始初始化 RabbitMQ 队列...");
        
        try {
            declareDeadLetterQueue();
            declareDelayQueue();
            declareDispatchTaskQueue();
            
            log.info("RabbitMQ 队列初始化完成");
        } catch (Exception e) {
            log.error("RabbitMQ 队列初始化失败", e);
        }
    }

    private void declareDeadLetterQueue() {
        FanoutExchange exchange = new FanoutExchange(MqConstants.DEAD_LETTER_EXCHANGE, true, false);
        Queue queue = new Queue(MqConstants.DEAD_LETTER_QUEUE, true);
        
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
        
        log.info("声明死信队列: {}", MqConstants.DEAD_LETTER_QUEUE);
    }

    private void declareDelayQueue() {
        DirectExchange exchange = new DirectExchange(MqConstants.DELAY_EXCHANGE, true, false);
        Queue queue = QueueBuilder.durable(MqConstants.DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", MqConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "")
                .build();
        
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(MqConstants.DELAY_ROUTING_KEY));
        
        log.info("声明延迟队列: {}", MqConstants.DELAY_QUEUE);
    }

    private void declareDispatchTaskQueue() {
        DirectExchange exchange = new DirectExchange(MqConstants.DISPATCH_TASK_EXCHANGE, true, false);
        Queue queue = new Queue(MqConstants.DISPATCH_TASK_QUEUE, true);
        
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(MqConstants.DISPATCH_TASK_ROUTING_KEY));
        
        log.info("声明调度任务队列: {}", MqConstants.DISPATCH_TASK_QUEUE);
    }
}
