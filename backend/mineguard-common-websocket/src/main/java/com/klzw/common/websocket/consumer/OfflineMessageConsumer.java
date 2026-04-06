package com.klzw.common.websocket.consumer;

import com.klzw.common.core.util.JsonUtils;
import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.MessagePushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OfflineMessageConsumer {

    private final MessagePushService messagePushService;
    private final OnlineUserManager onlineUserManager;

    public OfflineMessageConsumer(MessagePushService messagePushService,
                                  OnlineUserManager onlineUserManager) {
        this.messagePushService = messagePushService;
        this.onlineUserManager = onlineUserManager;
    }

    @RabbitListener(queues = "#{offlineMessageQueue}", containerFactory = "rabbitListenerContainerFactory")
    public void handleOfflineMessage(@Payload Message message,
                                     Channel channel,
                                     @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            String userId = extractUserIdFromRoutingKey(routingKey);

            if (userId == null) {
                log.warn("无法从路由键提取用户ID: routingKey={}", routingKey);
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.info("收到离线消息: userId={}, messageId={}", userId, message.getMessageId());

            if (onlineUserManager.isOnline(userId)) {
                messagePushService.pushToUser(userId, message);
                log.info("离线消息推送成功: userId={}, messageId={}", userId, message.getMessageId());
            } else {
                log.debug("用户仍不在线，消息已在MongoDB中保存: userId={}, messageId={}",
                        userId, message.getMessageId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理离线消息失败: error={}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private String extractUserIdFromRoutingKey(String routingKey) {
        if (routingKey == null || !routingKey.startsWith(MqConstants.OFFLINE_MESSAGE_ROUTING_KEY_PREFIX)) {
            return null;
        }
        return routingKey.substring(MqConstants.OFFLINE_MESSAGE_ROUTING_KEY_PREFIX.length());
    }
}
