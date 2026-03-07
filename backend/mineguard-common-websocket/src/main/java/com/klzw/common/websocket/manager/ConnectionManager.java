package com.klzw.common.websocket.manager;

import com.klzw.common.websocket.constant.WebSocketResultCode;
import com.klzw.common.websocket.domain.ConnectionInfo;
import com.klzw.common.websocket.exception.WebSocketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConnectionManager {
    private final Map<String, ConnectionInfo> sessionMap = new ConcurrentHashMap<>();
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public void addConnection(WebSocketSession session, String userId, String username, String role) {
        ConnectionInfo connectionInfo = ConnectionInfo.create(session, userId, username, role);
        sessionMap.put(session.getId(), connectionInfo);
        userSessionMap.put(userId, session.getId());
        log.info("WebSocket连接建立: sessionId={}, userId={}, username={}, role={}", 
                session.getId(), userId, username, role);
    }

    public void removeConnection(String sessionId) {
        ConnectionInfo connectionInfo = sessionMap.remove(sessionId);
        if (connectionInfo != null) {
            userSessionMap.remove(connectionInfo.getUserId());
            log.info("WebSocket连接断开: sessionId={}, userId={}", sessionId, connectionInfo.getUserId());
        }
    }

    public ConnectionInfo getConnection(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public ConnectionInfo getConnectionByUserId(String userId) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            return sessionMap.get(sessionId);
        }
        return null;
    }

    public boolean isOnline(String userId) {
        return userSessionMap.containsKey(userId);
    }

    public WebSocketSession getSession(String sessionId) {
        ConnectionInfo connectionInfo = sessionMap.get(sessionId);
        return connectionInfo != null ? connectionInfo.getSession() : null;
    }

    public WebSocketSession getSessionByUserId(String userId) {
        ConnectionInfo connectionInfo = getConnectionByUserId(userId);
        return connectionInfo != null ? connectionInfo.getSession() : null;
    }

    public void updateLastActiveTime(String sessionId) {
        ConnectionInfo connectionInfo = sessionMap.get(sessionId);
        if (connectionInfo != null) {
            connectionInfo.updateLastActiveTime();
        }
    }

    public void subscribeTopic(String sessionId, String topic) {
        ConnectionInfo connectionInfo = sessionMap.get(sessionId);
        if (connectionInfo != null) {
            connectionInfo.subscribeTopic(topic);
            log.info("订阅主题成功: sessionId={}, topic={}", sessionId, topic);
        } else {
            throw new WebSocketException(WebSocketResultCode.SESSION_NOT_FOUND, "会话不存在: " + sessionId);
        }
    }

    public void unsubscribeTopic(String sessionId, String topic) {
        ConnectionInfo connectionInfo = sessionMap.get(sessionId);
        if (connectionInfo != null) {
            connectionInfo.unsubscribeTopic(topic);
            log.info("取消订阅主题: sessionId={}, topic={}", sessionId, topic);
        }
    }

    public Collection<ConnectionInfo> getAllConnections() {
        return sessionMap.values();
    }

    public int getOnlineCount() {
        return sessionMap.size();
    }

    public void clear() {
        sessionMap.clear();
        userSessionMap.clear();
        log.info("清空所有WebSocket连接");
    }
}
