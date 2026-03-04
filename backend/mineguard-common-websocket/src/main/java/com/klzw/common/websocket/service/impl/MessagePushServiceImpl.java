package com.klzw.common.websocket.service.impl;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.service.MessagePushService;
import com.klzw.common.websocket.util.MessageBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessagePushServiceImpl implements MessagePushService {
    private final MessageManager messageManager;

    public MessagePushServiceImpl(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void pushToUser(String userId, Message message) {
        log.info("推送消息到用户: userId={}, messageId={}", userId, message.getMessageId());
        messageManager.sendMessageToUser(userId, message);
    }

    @Override
    public void broadcast(Message message) {
        log.info("广播消息: messageId={}", message.getMessageId());
        messageManager.broadcast(message);
    }

    @Override
    public void multicast(List<String> userIds, Message message) {
        log.info("群播消息: messageId={}, userCount={}", message.getMessageId(), userIds.size());
        messageManager.multicast(userIds, message);
    }

    @Override
    public void pushToRole(String role, Message message) {
        log.info("推送消息到角色: role={}, messageId={}", role, message.getMessageId());
        messageManager.sendToRole(role, message);
    }

    @Override
    public void pushToTopic(String topic, Message message) {
        log.info("推送消息到主题: topic={}, messageId={}", topic, message.getMessageId());
        messageManager.sendToTopic(topic, message);
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

        pushToUser(userId, message);
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

        pushToUser(userId, message);
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

        pushToUser(userId, message);
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

        pushToUser(userId, message);
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
}
