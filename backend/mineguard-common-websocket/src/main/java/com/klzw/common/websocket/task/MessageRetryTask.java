package com.klzw.common.websocket.task;

import com.klzw.common.websocket.config.WebSocketProperties;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.enums.MessagePriority;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.service.MessageHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageRetryTask {
    private final MessageHistoryService messageHistoryService;
    private final MessageManager messageManager;
    private final WebSocketProperties webSocketProperties;

    public MessageRetryTask(MessageHistoryService messageHistoryService,
                           MessageManager messageManager,
                           WebSocketProperties webSocketProperties) {
        this.messageHistoryService = messageHistoryService;
        this.messageManager = messageManager;
        this.webSocketProperties = webSocketProperties;
    }

    @Scheduled(fixedRate = 30000)
    public void retryPendingMessages() {
        int maxRetry = webSocketProperties.getReconnectMaxRetries();
        List<MessageHistory> pendingMessages = messageHistoryService.getPendingRetryMessages(maxRetry);
        
        if (!pendingMessages.isEmpty()) {
            log.info("开始重试待发送消息，数量: {}", pendingMessages.size());
            
            for (MessageHistory history : pendingMessages) {
                try {
                    messageManager.sendMessageToUser(history.getReceiver(), convertToMessage(history));
                    messageHistoryService.markAsDelivered(history.getMessageId());
                    log.debug("消息重试成功: messageId={}", history.getMessageId());
                } catch (Exception e) {
                    messageHistoryService.incrementRetryCount(history.getMessageId());
                    log.warn("消息重试失败: messageId={}, retryCount={}, error={}", 
                            history.getMessageId(), history.getRetryCount() + 1, e.getMessage());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredMessages() {
        log.info("开始清理过期消息");
        messageHistoryService.deleteExpiredMessages();
    }

    private Message convertToMessage(MessageHistory history) {
        return Message.builder()
                .messageId(history.getMessageId())
                .messageType(MessageTypeEnum.fromCode(history.getMessageType()))
                .sender(history.getSender())
                .receiver(history.getReceiver())
                .timestamp(history.getTimestamp())
                .priority(MessagePriority.valueOf(history.getPriority()))
                .requireAck(history.getRequireAck())
                .content((Map<String, Object>) history.getContent())
                .build();
    }
}
