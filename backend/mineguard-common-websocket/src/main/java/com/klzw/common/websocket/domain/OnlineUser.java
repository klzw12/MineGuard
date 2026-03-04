package com.klzw.common.websocket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUser {
    private String userId;
    private String username;
    private String role;
    private String sessionId;
    private LocalDateTime connectTime;
    private LocalDateTime lastActiveTime;
    private String clientIp;

    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
}
