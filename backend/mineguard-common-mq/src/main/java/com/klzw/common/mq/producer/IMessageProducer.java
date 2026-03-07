package com.klzw.common.mq.producer;

import java.util.List;

/**
 * 消息发送接口
 */
public interface IMessageProducer {

    /**
     * 发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     */
    void sendMessage(String exchange, String routingKey, Object message);

    /**
     * 发送延迟消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     * @param delayMillis 延迟时间（毫秒）
     */
    void sendDelayMessage(String exchange, String routingKey, Object message, long delayMillis);

    /**
     * 发送事务消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     * @return 是否发送成功
     */
    boolean sendTransactionMessage(String exchange, String routingKey, Object message);

    /**
     * 批量发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param messages 消息内容列表
     */
    void sendBatchMessage(String exchange, String routingKey, List<Object> messages);

}
