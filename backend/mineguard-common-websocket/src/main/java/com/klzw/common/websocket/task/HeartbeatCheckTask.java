package com.klzw.common.websocket.task;

import com.klzw.common.websocket.properties.WebSocketProperties;
import com.klzw.common.websocket.domain.ConnectionInfo;
import com.klzw.common.websocket.manager.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Slf4j
@Component
public class HeartbeatCheckTask {
    private final ConnectionManager connectionManager;
    private final WebSocketProperties webSocketProperties;

    public HeartbeatCheckTask(ConnectionManager connectionManager, WebSocketProperties webSocketProperties) {
        this.connectionManager = connectionManager;
        this.webSocketProperties = webSocketProperties;
    }

    @Scheduled(fixedRateString = "${websocket.heartbeat-interval:30000}")
    public void checkHeartbeat() {
        log.debug("开始心跳检测，当前连接数: {}", connectionManager.getOnlineCount());
        
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        int timeoutCount = 0;
        
        for (ConnectionInfo connectionInfo : connections) {
            long inactiveSeconds = ChronoUnit.SECONDS.between(
                    connectionInfo.getLastActiveTime(), 
                    LocalDateTime.now()
            );
            
            long timeoutSeconds = webSocketProperties.getHeartbeatTimeout() / 1000;
            
            if (inactiveSeconds > timeoutSeconds) {
                log.warn("连接超时，准备关闭: sessionId={}, userId={}, inactiveSeconds={}", 
                        connectionInfo.getSessionId(), 
                        connectionInfo.getUserId(), 
                        inactiveSeconds);
                
                try {
                    if (connectionInfo.getSession() != null && connectionInfo.getSession().isOpen()) {
                        connectionInfo.getSession().close();
                    }
                } catch (Exception e) {
                    log.error("关闭超时连接失败: sessionId={}", connectionInfo.getSessionId(), e);
                }
                
                timeoutCount++;
            }
        }
        
        if (timeoutCount > 0) {
            log.info("心跳检测完成，关闭超时连接数: {}", timeoutCount);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void resetRateLimit() {
        log.debug("重置消息频率限制");
    }
}
