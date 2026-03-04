package com.klzw.common.websocket.service.impl;

import com.klzw.common.websocket.config.WebSocketProperties;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageStatusEnum;
import com.klzw.common.websocket.repository.MessageHistoryRepository;
import com.klzw.common.websocket.service.MessageHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessageHistoryServiceImpl implements MessageHistoryService {
    private final MessageHistoryRepository messageHistoryRepository;
    private final WebSocketProperties webSocketProperties;

    public MessageHistoryServiceImpl(MessageHistoryRepository messageHistoryRepository,
                                    WebSocketProperties webSocketProperties) {
        this.messageHistoryRepository = messageHistoryRepository;
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public MessageHistory saveMessage(Message message) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(webSocketProperties.getOfflineMessageExpireDays());
        
        MessageHistory history = MessageHistory.builder()
                .messageId(message.getMessageId())
                .messageType(message.getMessageType().getCode())
                .sender(message.getSender())
                .receiver(message.getReceiver())
                .timestamp(message.getTimestamp())
                .priority(message.getPriority().getCode())
                .requireAck(message.getRequireAck())
                .content(message.getContent())
                .status(MessageStatusEnum.PENDING.getCode())
                .sendTime(now)
                .retryCount(0)
                .createTime(now)
                .expireTime(expireTime)
                .build();
        
        MessageHistory saved = messageHistoryRepository.save(history);
        log.debug("保存消息历史: messageId={}, type={}, receiver={}", 
                saved.getMessageId(), saved.getMessageType(), saved.getReceiver());
        return saved;
    }

    @Override
    public MessageHistory updateMessageStatus(String messageId, String status) {
        Optional<MessageHistory> optional = messageHistoryRepository.findByMessageId(messageId);
        if (optional.isPresent()) {
            MessageHistory history = optional.get();
            history.setStatus(status);
            return messageHistoryRepository.save(history);
        }
        return null;
    }

    @Override
    public MessageHistory markAsDelivered(String messageId) {
        Optional<MessageHistory> optional = messageHistoryRepository.findByMessageId(messageId);
        if (optional.isPresent()) {
            MessageHistory history = optional.get();
            history.setStatus(MessageStatusEnum.DELIVERED.getCode());
            history.setDeliverTime(LocalDateTime.now());
            return messageHistoryRepository.save(history);
        }
        return null;
    }

    @Override
    public MessageHistory markAsRead(String messageId) {
        Optional<MessageHistory> optional = messageHistoryRepository.findByMessageId(messageId);
        if (optional.isPresent()) {
            MessageHistory history = optional.get();
            history.setStatus(MessageStatusEnum.READ.getCode());
            history.setReadTime(LocalDateTime.now());
            return messageHistoryRepository.save(history);
        }
        return null;
    }

    @Override
    public List<MessageHistory> getUndeliveredMessages(String receiver) {
        return messageHistoryRepository.findByReceiverAndDeliverTimeIsNull(receiver);
    }

    @Override
    public List<MessageHistory> getUnreadMessages(String receiver) {
        return messageHistoryRepository.findByReceiverAndStatus(receiver, MessageStatusEnum.DELIVERED.getCode());
    }

    @Override
    public Page<MessageHistory> getMessageHistory(String receiver, Pageable pageable) {
        return messageHistoryRepository.findByReceiverOrderByTimestampDesc(receiver, pageable);
    }

    @Override
    public Page<MessageHistory> getSentMessages(String sender, Pageable pageable) {
        return messageHistoryRepository.findBySenderOrderByTimestampDesc(sender, pageable);
    }

    @Override
    public void deleteExpiredMessages() {
        LocalDateTime now = LocalDateTime.now();
        messageHistoryRepository.deleteByExpireTimeBefore(now);
        log.info("删除过期消息: time={}", now);
    }

    @Override
    public List<MessageHistory> getPendingRetryMessages(int maxRetryCount) {
        return messageHistoryRepository.findByStatusAndRetryCountLessThan(
                MessageStatusEnum.PENDING.getCode(), maxRetryCount);
    }

    @Override
    public void incrementRetryCount(String messageId) {
        Optional<MessageHistory> optional = messageHistoryRepository.findByMessageId(messageId);
        if (optional.isPresent()) {
            MessageHistory history = optional.get();
            history.setRetryCount(history.getRetryCount() + 1);
            messageHistoryRepository.save(history);
        }
    }

    @Override
    public long getUnreadCount(String receiver) {
        return messageHistoryRepository.countByReceiverAndReadTimeIsNull(receiver);
    }

    @Override
    public MessageHistory saveOfflineMessage(String userId, Message message) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(webSocketProperties.getOfflineMessageExpireDays());
        
        MessageHistory history = MessageHistory.builder()
                .messageId(message.getMessageId())
                .messageType(message.getMessageType().getCode())
                .sender(message.getSender())
                .receiver(userId)
                .timestamp(message.getTimestamp())
                .priority(message.getPriority().getCode())
                .requireAck(message.getRequireAck())
                .content(message.getContent())
                .status(MessageStatusEnum.PENDING.getCode())
                .sendTime(now)
                .retryCount(0)
                .createTime(now)
                .expireTime(expireTime)
                .build();
        
        MessageHistory saved = messageHistoryRepository.save(history);
        log.info("保存离线消息: messageId={}, userId={}, type={}", 
                saved.getMessageId(), userId, saved.getMessageType());
        return saved;
    }

    @Override
    public List<MessageHistory> getOfflineMessages(String userId) {
        return messageHistoryRepository.findByReceiverAndDeliverTimeIsNull(userId);
    }

    @Override
    public void markOfflineMessageAsSent(String messageId) {
        Optional<MessageHistory> optional = messageHistoryRepository.findByMessageId(messageId);
        if (optional.isPresent()) {
            MessageHistory history = optional.get();
            history.setStatus(MessageStatusEnum.DELIVERED.getCode());
            history.setDeliverTime(LocalDateTime.now());
            messageHistoryRepository.save(history);
            log.info("标记离线消息已发送: messageId={}", messageId);
        }
    }
}
