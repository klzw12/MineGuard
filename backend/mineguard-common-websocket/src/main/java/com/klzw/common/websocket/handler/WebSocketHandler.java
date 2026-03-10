package com.klzw.common.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.core.util.EncryptUtils;
import com.klzw.common.websocket.properties.WebSocketProperties;
import com.klzw.common.websocket.constant.WebSocketResultCode;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.exception.WebSocketException;
import com.klzw.common.websocket.manager.ConnectionManager;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.SmartMessagePushService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final ConnectionManager connectionManager;
    private final OnlineUserManager onlineUserManager;
    private final MessageManager messageManager;
    private final JwtUtils jwtUtils;
    private final WebSocketProperties webSocketProperties;
    private final MessageHistoryService messageHistoryService;
    private final SmartMessagePushService smartMessagePushService;

    public WebSocketHandler(ConnectionManager connectionManager, 
                           OnlineUserManager onlineUserManager,
                           MessageManager messageManager,
                           JwtUtils jwtUtils,
                           WebSocketProperties webSocketProperties,
                           MessageHistoryService messageHistoryService,
                           SmartMessagePushService smartMessagePushService) {
        this.connectionManager = connectionManager;
        this.onlineUserManager = onlineUserManager;
        this.messageManager = messageManager;
        this.jwtUtils = jwtUtils;
        this.webSocketProperties = webSocketProperties;
        this.messageHistoryService = messageHistoryService;
        this.smartMessagePushService = smartMessagePushService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立: sessionId={}, remoteAddress={}", 
                session.getId(), session.getRemoteAddress());
        
        String token = getTokenFromSession(session);
        if (token == null || token.isEmpty()) {
            sendConnectionFailedMessage(session, "TOKEN_MISSING", "Token缺失");
            closeSession(session);
            return;
        }

        try {
            Map<String, String> userInfo = validateToken(token);
            if (userInfo == null) {
                sendConnectionFailedMessage(session, "TOKEN_INVALID", "Token无效或已过期");
                closeSession(session);
                return;
            }

            String userId = userInfo.get("userId");
            String username = userInfo.get("username");
            String role = userInfo.get("role");

            connectionManager.addConnection(session, userId, username, role);
            onlineUserManager.addOnlineUser(userId, username, role, session.getId(), 
                    session.getRemoteAddress() != null ? session.getRemoteAddress().toString() : "unknown");

            sendConnectionSuccessMessage(session, userId, username, role);
            
            smartMessagePushService.pushOfflineMessages(userId);
        } catch (Exception e) {
            log.error("WebSocket认证失败: sessionId={}", session.getId(), e);
            sendConnectionFailedMessage(session, "AUTHENTICATION_FAILED", "认证失败");
            closeSession(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            // 检查是否需要解密消息
            if (webSocketProperties.isUseEncryption() && !payload.isEmpty()) {
                try {
                    payload = EncryptUtils.decrypt(payload, webSocketProperties.getEncryptionKey());
                    log.debug("消息解密成功: sessionId={}", session.getId());
                } catch (Exception e) {
                    log.error("消息解密失败: sessionId={}", session.getId(), e);
                    sendErrorMessage(session, "ENCRYPTION_ERROR", "消息解密失败");
                    return;
                }
            }

            JSONObject jsonMessage = JSON.parseObject(payload);
            String messageType = jsonMessage.getString("messageType");

            if (messageType == null) {
                sendErrorMessage(session, "MESSAGE_TYPE_UNKNOWN", "消息类型缺失");
                return;
            }

            connectionManager.updateLastActiveTime(session.getId());
            
            MessageTypeEnum typeEnum = MessageTypeEnum.fromCode(messageType);
            if (typeEnum == null) {
                sendErrorMessage(session, "MESSAGE_TYPE_UNKNOWN", "未知的消息类型: " + messageType);
                return;
            }

            switch (typeEnum) {
                case HEARTBEAT:
                    handleHeartbeat(session, jsonMessage);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(session, jsonMessage);
                    break;
                case AUTH:
                    handleAuth(session, jsonMessage);
                    break;
                case ACK:
                    handleAck(session, jsonMessage);
                    break;
                case ONLINE_STATUS:
                    handleOnlineStatus(session, jsonMessage);
                    break;
                default:
                    handleMessage(session, jsonMessage, typeEnum);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}", session.getId(), e);
            sendErrorMessage(session, "MESSAGE_FORMAT_INVALID", "消息格式无效");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), status);
        
        var connectionInfo = connectionManager.getConnection(session.getId());
        if (connectionInfo != null) {
            onlineUserManager.removeOnlineUser(connectionInfo.getUserId());
        }
        connectionManager.removeConnection(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
    }

    private void handleHeartbeat(WebSocketSession session, JSONObject jsonMessage) {
        JSONObject content = jsonMessage.getJSONObject("content");
        if (content != null && "PING".equals(content.getString("type"))) {
            Map<String, Object> responseContent = new HashMap<>();
            responseContent.put("type", "PONG");

            Message response = Message.builder()
                    .messageId("MSG_HEARTBEAT_" + System.currentTimeMillis())
                    .messageType(MessageTypeEnum.HEARTBEAT)
                    .sender("system")
                    .receiver(getUserId(session.getId()))
                    .content(responseContent)
                    .build();

            messageManager.sendMessage(session.getId(), response);
        }
    }

    private void handleSubscribe(WebSocketSession session, JSONObject jsonMessage) {
        JSONObject content = jsonMessage.getJSONObject("content");
        if (content == null) {
            sendErrorMessage(session, "SUBSCRIBE_FAILED", "订阅内容缺失");
            return;
        }

        String action = content.getString("action");
        List<String> topics = content.getJSONArray("topics").toJavaList(String.class);

        if ("SUBSCRIBE".equals(action)) {
            for (String topic : topics) {
                connectionManager.subscribeTopic(session.getId(), topic);
            }
            sendSubscribeSuccessMessage(session, topics, "SUBSCRIBE");
        } else if ("UNSUBSCRIBE".equals(action)) {
            for (String topic : topics) {
                connectionManager.unsubscribeTopic(session.getId(), topic);
            }
            sendSubscribeSuccessMessage(session, topics, "UNSUBSCRIBE");
        }
    }

    private void handleAuth(WebSocketSession session, JSONObject jsonMessage) {
        // 认证已在连接建立时处理
        log.debug("收到认证消息: sessionId={}", session.getId());
    }

    private void handleAck(WebSocketSession session, JSONObject jsonMessage) {
        JSONObject content = jsonMessage.getJSONObject("content");
        if (content != null) {
            String originalMessageId = content.getString("originalMessageId");
            String status = content.getString("status");
            log.info("收到消息确认: sessionId={}, originalMessageId={}, status={}", 
                    session.getId(), originalMessageId, status);
        }
    }

    private void handleOnlineStatus(WebSocketSession session, JSONObject jsonMessage) {
        JSONObject content = jsonMessage.getJSONObject("content");
        if (content != null && "QUERY".equals(content.getString("action"))) {
            List<String> userIds = content.getJSONArray("userIds").toJavaList(String.class);
            Map<String, Object> result = onlineUserManager.getOnlineUserStatus(userIds);

            Message response = Message.builder()
                    .messageId("MSG_ONLINE_" + System.currentTimeMillis())
                    .messageType(MessageTypeEnum.ONLINE_STATUS)
                    .sender("system")
                    .receiver(getUserId(session.getId()))
                    .content(result)
                    .build();

            messageManager.sendMessage(session.getId(), response);
        }
    }

    private void handleMessage(WebSocketSession session, JSONObject jsonMessage, MessageTypeEnum typeEnum) {
        // 处理其他类型的消息
        log.info("处理消息: sessionId={}, type={}", session.getId(), typeEnum.getCode());
    }

    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }

    private Map<String, String> validateToken(String token) {
        try {
            Claims claims = jwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", String.valueOf(userId));
            userInfo.put("username", username);
            userInfo.put("role", claims.get("role", String.class) != null ? claims.get("role", String.class) : "USER");
            return userInfo;
        } catch (AuthException e) {
            log.error("Token验证失败: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Token验证异常: {}", e.getMessage(), e);
            return null;
        }
    }

    private void sendConnectionSuccessMessage(WebSocketSession session, String userId, String username, String role) {
        Map<String, Object> content = new HashMap<>();
        content.put("status", "SUCCESS");
        content.put("userId", userId);
        content.put("username", username);
        content.put("role", role);
        content.put("sessionId", session.getId());

        Message message = Message.builder()
                .messageId("MSG_CONN_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.AUTH)
                .sender("system")
                .receiver(userId)
                .content(content)
                .build();

        messageManager.sendMessage(session.getId(), message);
    }

    private void sendConnectionFailedMessage(WebSocketSession session, String errorCode, String errorMessage) {
        Map<String, Object> content = new HashMap<>();
        content.put("status", "FAILED");
        content.put("errorCode", errorCode);
        content.put("errorMessage", errorMessage);

        Message message = Message.builder()
                .messageId("MSG_CONN_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.AUTH)
                .sender("system")
                .receiver("unknown")
                .content(content)
                .build();

        try {
            messageManager.sendMessage(session.getId(), message);
        } catch (Exception e) {
            log.error("发送连接失败消息失败: sessionId={}", session.getId(), e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorCode, String errorMessage) {
        Map<String, Object> content = new HashMap<>();
        content.put("errorCode", errorCode);
        content.put("errorMessage", errorMessage);

        Message message = Message.builder()
                .messageId("MSG_ERROR_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.ERROR)
                .sender("system")
                .receiver(getUserId(session.getId()))
                .content(content)
                .build();

        messageManager.sendMessage(session.getId(), message);
    }

    private void sendSubscribeSuccessMessage(WebSocketSession session, List<String> topics, String action) {
        Map<String, Object> content = new HashMap<>();
        content.put("status", "SUCCESS");
        content.put("topics", topics);
        content.put("action", action);

        Message message = Message.builder()
                .messageId("MSG_SUB_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.SUBSCRIBE)
                .sender("system")
                .receiver(getUserId(session.getId()))
                .content(content)
                .build();

        messageManager.sendMessage(session.getId(), message);
    }

    private String getUserId(String sessionId) {
        var connectionInfo = connectionManager.getConnection(sessionId);
        return connectionInfo != null ? connectionInfo.getUserId() : "unknown";
    }

    private void closeSession(WebSocketSession session) {
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭WebSocket会话失败: sessionId={}", session.getId(), e);
        }
    }
}
