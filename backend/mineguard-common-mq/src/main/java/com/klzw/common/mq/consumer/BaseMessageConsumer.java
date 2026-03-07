package com.klzw.common.mq.consumer;

import com.klzw.common.mq.constant.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.klzw.common.mq.constant.MqResultCode;
import com.klzw.common.mq.exception.MqException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础消息消费者实现类
 */
@Slf4j
public abstract class BaseMessageConsumer implements IMessageConsumer {

    private RabbitTemplate rabbitTemplate;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 默认构造函数
     */
    public BaseMessageConsumer() {
    }

    /**
     * 构造函数注入（用于测试）
     */
    public BaseMessageConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 设置 RabbitTemplate（用于测试）
     */
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void consume(Message message) {
        AtomicInteger retryCount = getRetryCount(message);
        try {
            log.info("开始处理消息，exchange: {}, routingKey: {}, retryCount: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(),
                retryCount.get());
            doConsume(message);
            log.info("消息处理成功，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
        } catch (AmqpException e) {
            log.error("RabbitMQ 消息处理异常，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
            handleException(message, retryCount, e);
        } catch (Exception e) {
            log.error("消息处理失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
            handleException(message, retryCount, e);
        }
    }

    /**
     * 实际处理消息的方法
     * @param message 消息
     * @throws Exception 处理异常
     */
    protected abstract void doConsume(Message message) throws Exception;

    /**
     * 获取重试次数
     * @param message 消息
     * @return 重试次数
     */
    private AtomicInteger getRetryCount(Message message) {
        Integer count = (Integer) message.getMessageProperties().getHeaders().get("retryCount");
        if (count == null) {
            count = 0;
        }
        return new AtomicInteger(count);
    }

    /**
     * 处理异常
     * @param message 消息
     * @param retryCount 重试次数
     * @param e 异常
     */
    private void handleException(Message message, AtomicInteger retryCount, Exception e) {
        int currentRetry = retryCount.incrementAndGet();
        if (currentRetry <= MAX_RETRY_COUNT) {
            log.warn("消息处理失败，开始第{}次重试，exchange: {}, routingKey: {}", 
                currentRetry, message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            message.getMessageProperties().getHeaders().put("retryCount", currentRetry);
            try {
                rabbitTemplate.send(message.getMessageProperties().getReceivedExchange(),
                        message.getMessageProperties().getReceivedRoutingKey(),
                        message);
                log.info("消息重试发送成功，exchange: {}, routingKey: {}, retryCount: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(),
                    currentRetry);
            } catch (AmqpException ex) {
                log.error("消息重试发送失败，exchange: {}, routingKey: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(), ex);
                sendToDeadLetterQueue(message);
            } catch (Exception ex) {
                log.error("消息重试发送失败，exchange: {}, routingKey: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(), ex);
                sendToDeadLetterQueue(message);
            }
        } else {
            log.error("消息重试次数超过限制，发送到死信队列，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            sendToDeadLetterQueue(message);
            throw new MqException(MqResultCode.CONSUMER_RETRY_EXCEEDED.getCode(), "消息重试次数超过限制");
        }
    }

    /**
     * 发送到死信队列
     * @param message 消息
     */
    private void sendToDeadLetterQueue(Message message) {
        try {
            log.info("发送消息到死信队列，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            rabbitTemplate.send(MqConstants.DEAD_LETTER_EXCHANGE, "", message);
            log.info("消息发送到死信队列成功");
        } catch (AmqpException e) {
            log.error("消息发送到死信队列失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
        } catch (Exception e) {
            log.error("消息发送到死信队列失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
        }
    }

}
