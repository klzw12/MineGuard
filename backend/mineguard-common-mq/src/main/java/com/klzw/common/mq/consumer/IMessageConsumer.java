package com.klzw.common.mq.consumer;

import org.springframework.amqp.core.Message;

/**
 * 消息消费接口
 */
public interface IMessageConsumer {

    /**
     * 消费消息
     * @param message 消息
     */
    void consume(Message message);

}
