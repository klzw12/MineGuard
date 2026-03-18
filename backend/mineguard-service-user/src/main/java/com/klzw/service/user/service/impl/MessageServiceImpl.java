package com.klzw.service.user.service.impl;

import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.service.user.dto.MessageDTO;
import com.klzw.service.user.entity.Message;
import com.klzw.service.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final IMessageProducer messageProducer;
    private final MessageManager messageManager;
    private final OnlineUserManager onlineUserManager;
    private final MongoTemplate mongoTemplate;

    @Override
    public void sendUnicastMessage(MessageDTO messageDTO) {
        // 生成消息ID
        if (messageDTO.getMessageId() == null) {
            messageDTO.setMessageId(UUID.randomUUID().toString());
        }

        // 保存消息到MongoDB
        Message message = convertToEntity(messageDTO);
        message.setStatus("UNREAD");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        mongoTemplate.save(message);

        // 通过WebSocket发送消息给特定用户
        com.klzw.common.websocket.domain.Message webSocketMessage = convertToWebSocketMessage(messageDTO);
        try {
            messageManager.sendMessageToUser(messageDTO.getReceiver(), webSocketMessage);
        } catch (Exception e) {
            log.warn("WebSocket发送失败: receiver={}, error={}", messageDTO.getReceiver(), e.getMessage());
        }

        // 通过MQ发送消息（异步处理）
        messageProducer.sendMessage("message-exchange", "message.unicast", messageDTO);

        log.info("发送单播消息: messageId={}, receiver={}", messageDTO.getMessageId(), messageDTO.getReceiver());
    }

    @Override
    public void sendBroadcastMessage(MessageDTO messageDTO, List<String> userIds) {
        // 生成消息ID
        if (messageDTO.getMessageId() == null) {
            messageDTO.setMessageId(UUID.randomUUID().toString());
        }

        // 为每个用户创建消息记录
        for (String userId : userIds) {
            Message message = convertToEntity(messageDTO);
            message.setReceiver(userId);
            message.setStatus("UNREAD");
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());
            mongoTemplate.save(message);

            // 通过WebSocket发送消息给在线用户
            com.klzw.common.websocket.domain.Message webSocketMessage = convertToWebSocketMessage(messageDTO);
            try {
                messageManager.sendMessageToUser(userId, webSocketMessage);
            } catch (Exception e) {
                log.warn("WebSocket发送失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        // 通过MQ发送广播消息
        messageProducer.sendMessage("message-exchange", "message.broadcast", messageDTO);

        log.info("发送广播消息: messageId={}, userCount={}", messageDTO.getMessageId(), userIds.size());
    }

    @Override
    public void sendBroadcastMessageToAll(MessageDTO messageDTO) {
        // 生成消息ID
        if (messageDTO.getMessageId() == null) {
            messageDTO.setMessageId(UUID.randomUUID().toString());
        }

        // 获取所有在线用户
        List<String> onlineUserIds = onlineUserManager.getAllOnlineUsers().stream()
                .map(user -> user.getUserId())
                .collect(Collectors.toList());

        // 发送广播消息给所有在线用户
        sendBroadcastMessage(messageDTO, onlineUserIds);

        log.info("发送广播消息给所有在线用户: messageId={}, onlineUserCount={}", messageDTO.getMessageId(), onlineUserIds.size());
    }

    private com.klzw.common.websocket.domain.Message convertToWebSocketMessage(MessageDTO messageDTO) {
        com.klzw.common.websocket.domain.Message webSocketMessage = new com.klzw.common.websocket.domain.Message();
        webSocketMessage.setMessageId(messageDTO.getMessageId());
        webSocketMessage.setSender(messageDTO.getSender());
        webSocketMessage.setReceiver(messageDTO.getReceiver());
        webSocketMessage.setMessageType(MessageTypeEnum.SYSTEM_NOTICE);
        
        // 转换content为Map
        java.util.Map<String, Object> contentMap = new java.util.HashMap<>();
        contentMap.put("message", messageDTO.getContent());
        contentMap.put("businessId", messageDTO.getBusinessId());
        contentMap.put("businessType", messageDTO.getBusinessType());
        webSocketMessage.setContent(contentMap);
        
        // 转换priority为MessagePriority
        com.klzw.common.websocket.enums.MessagePriority priority = com.klzw.common.websocket.enums.MessagePriority.LOW;
        if ("HIGH".equals(messageDTO.getPriority())) {
            priority = com.klzw.common.websocket.enums.MessagePriority.HIGH;
        } else if ("MEDIUM".equals(messageDTO.getPriority())) {
            priority = com.klzw.common.websocket.enums.MessagePriority.MEDIUM;
        }
        webSocketMessage.setPriority(priority);
        
        return webSocketMessage;
    }

    @Override
    public List<Message> getUserMessages(String userId, int page, int size) {
        Query query = new Query(Criteria.where("receiver").is(userId))
                .with(org.springframework.data.domain.PageRequest.of(page - 1, size))
                .with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Message.class);
    }

    @Override
    public void markMessageAsRead(String messageId) {
        Query query = new Query(Criteria.where("messageId").is(messageId));
        Update update = new Update()
                .set("status", "READ")
                .set("readAt", LocalDateTime.now())
                .set("updatedAt", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, Message.class);
    }

    @Override
    public void deleteMessage(String messageId) {
        Query query = new Query(Criteria.where("messageId").is(messageId));
        mongoTemplate.remove(query, Message.class);
    }

    @Override
    public long getUnreadMessageCount(String userId) {
        Query query = new Query(Criteria.where("receiver").is(userId).and("status").is("UNREAD"));
        return mongoTemplate.count(query, Message.class);
    }

    private Message convertToEntity(MessageDTO messageDTO) {
        Message message = new Message();
        message.setMessageId(messageDTO.getMessageId());
        message.setSender(messageDTO.getSender());
        message.setReceiver(messageDTO.getReceiver());
        message.setType(messageDTO.getType());
        message.setContent(messageDTO.getContent());
        message.setPriority(messageDTO.getPriority());
        message.setBusinessId(messageDTO.getBusinessId());
        message.setBusinessType(messageDTO.getBusinessType());
        return message;
    }
}