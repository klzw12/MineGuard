package com.klzw.common.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {
    private boolean enabled = true;
    private int heartbeatInterval = 30000;
    private int heartbeatTimeout = 60000;
    private int maxConnections = 10000;
    private int maxTopicsPerUser = 50;
    private int maxMessagesPerMinute = 100;
    private int reconnectMaxRetries = 5;
    private int offlineMessageExpireDays = 7;
    private boolean useEncryption = false;
    private String encryptionKey = "";
    private boolean enableMessagePersistence = false;
    private String[] allowedOrigins = new String[0];
}
