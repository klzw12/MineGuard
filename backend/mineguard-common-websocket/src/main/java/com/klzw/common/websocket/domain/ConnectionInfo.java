package com.klzw.common.websocket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionInfo {
    private String sessionId;
    private String userId;
    private String username;
    private String role;
    private WebSocketSession session;
    private LocalDateTime connectTime;
    private LocalDateTime lastActiveTime;
    private String clientIp;
    private Set<String> subscribedTopics;

    public static ConnectionInfo create(WebSocketSession session, String userId, String username, String role) {
        return ConnectionInfo.builder()
                .sessionId(session.getId())
                .userId(userId)
                .username(username)
                .role(role)
                .session(session)
                .connectTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .clientIp(session.getRemoteAddress() != null ? session.getRemoteAddress().toString() : "unknown")
                .subscribedTopics(ConcurrentHashMap.newKeySet())
                .build();
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    public void subscribeTopic(String topic) {
        if (subscribedTopics != null) {
            subscribedTopics.add(topic);
        }
    }

    public void unsubscribeTopic(String topic) {
        if (subscribedTopics != null) {
            subscribedTopics.remove(topic);
        }
    }

    public boolean isSubscribed(String topic) {
        return subscribedTopics != null && subscribedTopics.contains(topic);
    }
}
