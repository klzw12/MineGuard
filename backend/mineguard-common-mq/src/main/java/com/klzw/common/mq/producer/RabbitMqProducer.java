package com.klzw.common.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.klzw.common.mq.constant.MqResultCode;
import com.klzw.common.mq.exception.MqException;

import java.util.UUID;

/**
 * RabbitMQ 消息发送实现类
 */
@Slf4j
@Component
public class RabbitMqProducer implements IMessageProducer {


    private final RabbitTemplate rabbitTemplate;

    public RabbitMqProducer(RabbitTemplate rabbitTemplate) {
        log.info("初始化 RabbitMqProducer");
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMessage(String exchange, String routingKey, Object message) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            log.info("开始发送消息，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            log.info("消息发送成功，correlationId: {}", correlationData.getId());
        } catch (AmqpException e) {
            log.error("RabbitMQ 消息发送异常，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new MqException(MqResultCode.PRODUCER_SEND_ERROR.getCode(), "消息发送失败", e);
        } catch (Exception e) {
            log.error("消息发送失败，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new MqException(MqResultCode.MQ_ERROR.getCode(), "消息队列操作失败", e);
        }
    }

    @Override
    public void sendDelayMessage(String exchange, String routingKey, Object message, long delayMillis) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            log.info("开始发送延迟消息，exchange: {}, routingKey: {}, message: {}, delayMillis: {}", exchange, routingKey, message, delayMillis);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, 
                msg -> {
                    msg.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return msg;
                },
                correlationData
            );
            log.info("延迟消息发送成功，correlationId: {}", correlationData.getId());
        } catch (AmqpException e) {
            log.error("RabbitMQ 延迟消息发送异常，exchange: {}, routingKey: {}, message: {}, delayMillis: {}", exchange, routingKey, message, delayMillis, e);
            throw new MqException(MqResultCode.PRODUCER_SEND_ERROR.getCode(), "延迟消息发送失败", e);
        } catch (Exception e) {
            log.error("延迟消息发送失败，exchange: {}, routingKey: {}, message: {}, delayMillis: {}", exchange, routingKey, message, delayMillis, e);
            throw new MqException(MqResultCode.MQ_ERROR.getCode(), "消息队列操作失败", e);
        }
    }

    @Override
    @Transactional
    public boolean sendTransactionMessage(String exchange, String routingKey, Object message) {
        try {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            log.info("开始发送事务消息，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            log.info("事务消息发送成功，correlationId: {}", correlationData.getId());
            return true;
        } catch (AmqpException e) {
            log.error("RabbitMQ 事务消息发送异常，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new MqException(MqResultCode.PRODUCER_TRANSACTION_ERROR.getCode(), "事务消息处理失败", e);
        } catch (Exception e) {
            log.error("事务消息发送失败，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new MqException(MqResultCode.MQ_ERROR.getCode(), "消息队列操作失败", e);
        }
    }

    @Override
    public void sendBatchMessage(String exchange, String routingKey, List<Object> messages) {
        if (messages == null || messages.isEmpty()) {
            log.info("批量发送消息，消息列表为空");
            return;
        }
        
        log.info("开始批量发送消息，exchange: {}, routingKey: {}, 消息数量: {}", exchange, routingKey, messages.size());
        
        int successCount = 0;
        int failCount = 0;
        
        try {
            for (Object message : messages) {
                try {
                    CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
                    rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
                    successCount++;
                    log.debug("批量发送消息成功，correlationId: {}", correlationData.getId());
                } catch (Exception e) {
                    failCount++;
                    log.error("批量发送消息失败，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
                }
            }
            
            log.info("批量发送消息完成，成功发送 {} 条消息，失败 {} 条消息", successCount, failCount);
        } catch (Exception e) {
            log.error("批量发送消息异常，exchange: {}, routingKey: {}", exchange, routingKey, e);
            throw new MqException(MqResultCode.PRODUCER_SEND_ERROR.getCode(), "批量消息发送失败", e);
        }
    }

}
