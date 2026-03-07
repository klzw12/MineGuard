package com.klzw.common.websocket.service.impl;

import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.MessagePushService;
import com.klzw.common.websocket.service.SmartMessagePushService;
import com.klzw.common.websocket.util.MessageBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SmartMessagePushServiceImpl implements SmartMessagePushService {
    
    private final MessagePushService messagePushService;
    private final MessageManager messageManager;
    private final OnlineUserManager onlineUserManager;
    private final MessageHistoryService messageHistoryService;
    private final IMessageProducer messageProducer;

    public SmartMessagePushServiceImpl(MessagePushService messagePushService,
                                       MessageManager messageManager,
                                       OnlineUserManager onlineUserManager,
                                       MessageHistoryService messageHistoryService,
                                       IMessageProducer messageProducer) {
        this.messagePushService = messagePushService;
        this.messageManager = messageManager;
        this.onlineUserManager = onlineUserManager;
        this.messageHistoryService = messageHistoryService;
        this.messageProducer = messageProducer;
    }

    @Override
    public void pushToUser(String userId, Message message) {
        if (onlineUserManager.isOnline(userId)) {
            messagePushService.pushToUser(userId, message);
        } else {
            log.debug("用户不在线，跳过推送: userId={}, messageId={}", userId, message.getMessageId());
        }
    }

    @Override
    public void pushToUserWithOffline(String userId, Message message) {
        if (onlineUserManager.isOnline(userId)) {
            messagePushService.pushToUser(userId, message);
            log.info("WebSocket实时推送成功: userId={}, messageId={}", userId, message.getMessageId());
        } else {
            saveAndSendToMq(userId, message);
        }
    }

    @Override
    public void broadcast(Message message) {
        messagePushService.broadcast(message);
    }

    @Override
    public void multicast(List<String> userIds, Message message) {
        for (String userId : userIds) {
            pushToUserWithOffline(userId, message);
        }
    }

    @Override
    public void pushToRole(String role, Message message) {
        messagePushService.pushToRole(role, message);
    }

    @Override
    public void pushToTopic(String topic, Message message) {
        messagePushService.pushToTopic(topic, message);
    }

    @Override
    public void pushVehicleStatus(String userId, Long carId, Map<String, Object> vehicleData) {
        Map<String, Object> content = new HashMap<>();
        content.put("carId", carId);
        content.putAll(vehicleData);

        Message message = MessageBuilder.builder()
                .messageType(MessageTypeEnum.VEHICLE_STATUS)
                .sender("system")
                .receiver(userId)
                .content(content)
                .build();

        pushToUserWithOffline(userId, message);
    }

    @Override
    public void pushWarningNotification(String userId, Long warningId, Map<String, Object> warningData) {
        Map<String, Object> content = new HashMap<>();
        content.put("warningId", warningId);
        content.putAll(warningData);

        Message message = MessageBuilder.builder()
                .messageType(MessageTypeEnum.WARNING_NOTIFICATION)
                .sender("system")
                .receiver(userId)
                .requireAck(true)
                .content(content)
                .build();

        pushToUserWithOffline(userId, message);
    }

    @Override
    public void pushDispatchCommand(String userId, Long commandId, Map<String, Object> commandData) {
        Map<String, Object> content = new HashMap<>();
        content.put("commandId", commandId);
        content.putAll(commandData);

        Message message = MessageBuilder.builder()
                .messageType(MessageTypeEnum.DISPATCH_COMMAND)
                .sender("dispatcher")
                .receiver(userId)
                .requireAck(true)
                .content(content)
                .build();

        pushToUserWithOffline(userId, message);
    }

    @Override
    public void pushTripUpdate(String userId, Long tripId, Map<String, Object> tripData) {
        Map<String, Object> content = new HashMap<>();
        content.put("tripId", tripId);
        content.putAll(tripData);

        Message message = MessageBuilder.builder()
                .messageType(MessageTypeEnum.TRIP_UPDATE)
                .sender("system")
                .receiver(userId)
                .content(content)
                .build();

        pushToUserWithOffline(userId, message);
    }

    @Override
    public void pushSystemNotice(String title, String content, String type) {
        Map<String, Object> noticeContent = new HashMap<>();
        noticeContent.put("title", title);
        noticeContent.put("content", content);
        noticeContent.put("type", type);
        noticeContent.put("publishTime", System.currentTimeMillis());

        Message message = MessageBuilder.builder()
                .messageType(MessageTypeEnum.SYSTEM_NOTICE)
                .sender("admin")
                .receiver("all")
                .requireAck(true)
                .content(noticeContent)
                .build();

        broadcast(message);
    }

    @Override
    public void pushOfflineMessages(String userId) {
        if (!onlineUserManager.isOnline(userId)) {
            log.warn("用户不在线，无法推送离线消息: userId={}", userId);
            return;
        }
        
        List<MessageHistory> offlineMessages = messageHistoryService.getOfflineMessages(userId);
        if (offlineMessages == null || offlineMessages.isEmpty()) {
            log.debug("无离线消息: userId={}", userId);
            return;
        }
        
        log.info("开始推送离线消息: userId={}, count={}", userId, offlineMessages.size());
        
        for (MessageHistory history : offlineMessages) {
            try {
                Message message = convertToMessage(history);
                messagePushService.pushToUser(userId, message);
                messageHistoryService.markOfflineMessageAsSent(history.getMessageId());
                log.debug("离线消息推送成功: messageId={}", history.getMessageId());
            } catch (Exception e) {
                log.error("离线消息推送失败: messageId={}, error={}", history.getMessageId(), e.getMessage());
            }
        }
        
        log.info("离线消息推送完成: userId={}, count={}", userId, offlineMessages.size());
    }

    private void saveAndSendToMq(String userId, Message message) {
        messageHistoryService.saveOfflineMessage(userId, message);
        
        try {
            String routingKey = MqConstants.OFFLINE_MESSAGE_ROUTING_KEY_PREFIX + userId;
            messageProducer.sendMessage(MqConstants.OFFLINE_MESSAGE_EXCHANGE, routingKey, message);
            log.info("离线消息已发送到MQ: userId={}, messageId={}", userId, message.getMessageId());
        } catch (Exception e) {
            log.error("离线消息发送到MQ失败: userId={}, messageId={}, error={}", 
                    userId, message.getMessageId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Message convertToMessage(MessageHistory history) {
        return MessageBuilder.builder()
                .messageId(history.getMessageId())
                .messageType(MessageTypeEnum.fromCode(history.getMessageType()))
                .sender(history.getSender())
                .receiver(history.getReceiver())
                .timestamp(history.getTimestamp())
                .content((Map<String, Object>) history.getContent())
                .build();
    }
}
