package com.klzw.common.websocket.manager;

import com.alibaba.fastjson.JSON;
import com.klzw.common.core.util.EncryptUtils;
import com.klzw.common.websocket.properties.WebSocketProperties;
import com.klzw.common.websocket.constant.WebSocketResultCode;
import com.klzw.common.websocket.domain.ConnectionInfo;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.exception.WebSocketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MessageManager {
    private final ConnectionManager connectionManager;
    private final WebSocketProperties webSocketProperties;
    private final Map<String, AtomicInteger> userMessageCountMap = new ConcurrentHashMap<>();

    public MessageManager(ConnectionManager connectionManager, WebSocketProperties webSocketProperties) {
        this.connectionManager = connectionManager;
        this.webSocketProperties = webSocketProperties;
    }

    public void sendMessage(String sessionId, Message message) {
        WebSocketSession session = connectionManager.getSession(sessionId);
        if (session == null || !session.isOpen()) {
            throw new WebSocketException(WebSocketResultCode.SESSION_NOT_FOUND, "会话不存在或已关闭");
        }

        try {
            String messageJson = JSON.toJSONString(message);
            
            // 检查是否需要加密消息
            if (webSocketProperties.isUseEncryption() && !messageJson.isEmpty()) {
                try {
                    messageJson = EncryptUtils.encrypt(messageJson, webSocketProperties.getEncryptionKey());
                    log.debug("消息加密成功: sessionId={}, messageId={}", sessionId, message.getMessageId());
                } catch (Exception e) {
                    log.error("消息加密失败: sessionId={}, messageId={}", sessionId, message.getMessageId(), e);
                    throw new WebSocketException(WebSocketResultCode.ENCRYPTION_ERROR, "消息加密失败", e);
                }
            }
            
            session.sendMessage(new TextMessage(messageJson));
            log.debug("发送消息成功: sessionId={}, messageId={}, type={}", 
                    sessionId, message.getMessageId(), message.getMessageType());
        } catch (WebSocketException e) {
            throw e;
        } catch (IOException e) {
            log.error("发送消息失败: sessionId={}, messageId={}", sessionId, message.getMessageId(), e);
            throw new WebSocketException(WebSocketResultCode.MESSAGE_SEND_FAILED, "消息发送失败", e);
        } catch (Exception e) {
            log.error("发送消息异常: sessionId={}, messageId={}", sessionId, message.getMessageId(), e);
            throw new WebSocketException(WebSocketResultCode.MESSAGE_SEND_FAILED, "消息发送失败", e);
        }
    }

    public void sendMessageToUser(String userId, Message message) {
        WebSocketSession session = connectionManager.getSessionByUserId(userId);
        if (session == null || !session.isOpen()) {
            throw new WebSocketException(WebSocketResultCode.USER_NOT_ONLINE, "用户不在线: " + userId);
        }

        checkRateLimit(userId);
        sendMessage(session.getId(), message);
    }

    public void broadcast(Message message) {
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        for (ConnectionInfo connectionInfo : connections) {
            try {
                sendMessage(connectionInfo.getSessionId(), message);
            } catch (WebSocketException e) {
                log.warn("广播消息失败: sessionId={}, error={}", connectionInfo.getSessionId(), e.getMessage());
            }
        }
        log.info("广播消息完成: messageId={}, type={}, receiverCount={}", 
                message.getMessageId(), message.getMessageType(), connections.size());
    }

    public void multicast(List<String> userIds, Message message) {
        int successCount = 0;
        for (String userId : userIds) {
            try {
                sendMessageToUser(userId, message);
                successCount++;
            } catch (WebSocketException e) {
                log.warn("群播消息失败: userId={}, error={}", userId, e.getMessage());
            }
        }
        log.info("群播消息完成: messageId={}, type={}, successCount={}, totalCount={}", 
                message.getMessageId(), message.getMessageType(), successCount, userIds.size());
    }

    public void sendToRole(String role, Message message) {
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        int count = 0;
        
        for (ConnectionInfo connectionInfo : connections) {
            if (role.equals(connectionInfo.getRole())) {
                try {
                    sendMessage(connectionInfo.getSessionId(), message);
                    count++;
                } catch (WebSocketException e) {
                    log.warn("发送消息到角色失败: sessionId={}, role={}, error={}", 
                            connectionInfo.getSessionId(), role, e.getMessage());
                }
            }
        }
        
        log.info("发送消息到角色完成: messageId={}, type={}, role={}, count={}", 
                message.getMessageId(), message.getMessageType(), role, count);
    }

    public void sendToTopic(String topic, Message message) {
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        int count = 0;
        
        for (ConnectionInfo connectionInfo : connections) {
            if (connectionInfo.isSubscribed(topic)) {
                try {
                    sendMessage(connectionInfo.getSessionId(), message);
                    count++;
                } catch (WebSocketException e) {
                    log.warn("发送消息到主题失败: sessionId={}, topic={}, error={}", 
                            connectionInfo.getSessionId(), topic, e.getMessage());
                }
            }
        }
        
        log.info("发送消息到主题完成: messageId={}, type={}, topic={}, count={}", 
                message.getMessageId(), message.getMessageType(), topic, count);
    }

    private void checkRateLimit(String userId) {
        AtomicInteger count = userMessageCountMap.computeIfAbsent(userId, k -> new AtomicInteger(0));
        
        int maxMessages = webSocketProperties.getMaxMessagesPerMinute();
        if (count.get() >= maxMessages) {
            throw new WebSocketException(WebSocketResultCode.RATE_LIMIT_EXCEEDED, 
                    "消息频率超限，最大限制: " + maxMessages + "条/分钟");
        }
        
        count.incrementAndGet();
    }

    public void resetRateLimit() {
        userMessageCountMap.clear();
    }

    public void resetRateLimit(String userId) {
        userMessageCountMap.remove(userId);
    }
}
