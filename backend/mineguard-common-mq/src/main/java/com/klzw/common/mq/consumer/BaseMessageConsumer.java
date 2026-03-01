package com.klzw.common.mq.consumer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klzw.common.mq.exception.MqException;
import com.klzw.common.mq.exception.MqResultCode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础消息消费者实现类
 */
public abstract class BaseMessageConsumer implements IMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BaseMessageConsumer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public void consume(Message message) {
        AtomicInteger retryCount = getRetryCount(message);
        try {
            logger.info("开始处理消息，exchange: {}, routingKey: {}, retryCount: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(),
                retryCount.get());
            // 处理消息
            doConsume(message);
            logger.info("消息处理成功，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
        } catch (AmqpException e) {
            logger.error("RabbitMQ 消息处理异常，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
            // 处理异常
            handleException(message, retryCount, e);
        } catch (Exception e) {
            logger.error("消息处理失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
            // 处理异常
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
            // 重新发送消息
            logger.warn("消息处理失败，开始第{}次重试，exchange: {}, routingKey: {}", 
                currentRetry, message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            message.getMessageProperties().getHeaders().put("retryCount", currentRetry);
            try {
                rabbitTemplate.send(message.getMessageProperties().getReceivedExchange(),
                        message.getMessageProperties().getReceivedRoutingKey(),
                        message);
                logger.info("消息重试发送成功，exchange: {}, routingKey: {}, retryCount: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(),
                    currentRetry);
            } catch (AmqpException ex) {
                logger.error("消息重试发送失败，exchange: {}, routingKey: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(), ex);
                // 发送到死信队列
                sendToDeadLetterQueue(message);
            } catch (Exception ex) {
                logger.error("消息重试发送失败，exchange: {}, routingKey: {}", 
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(), ex);
                // 发送到死信队列
                sendToDeadLetterQueue(message);
            }
        } else {
            // 超过最大重试次数，发送到死信队列
            logger.error("消息重试次数超过限制，发送到死信队列，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            sendToDeadLetterQueue(message);
            // 可以在这里抛出异常，让调用方知道消息处理最终失败
            throw new MqException(MqResultCode.CONSUMER_RETRY_EXCEEDED.getCode(), "消息重试次数超过限制");
        }
    }

    /**
     * 发送到死信队列
     * @param message 消息
     */
    private void sendToDeadLetterQueue(Message message) {
        try {
            logger.info("发送消息到死信队列，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey());
            rabbitTemplate.send("dead.letter.exchange", "", message);
            logger.info("消息发送到死信队列成功");
        } catch (AmqpException e) {
            logger.error("消息发送到死信队列失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
        } catch (Exception e) {
            logger.error("消息发送到死信队列失败，exchange: {}, routingKey: {}", 
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(), e);
        }
    }

}
