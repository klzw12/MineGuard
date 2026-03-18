package com.klzw.common.websocket.config;

import com.klzw.common.websocket.handler.WebSocketHandler;
import com.klzw.common.websocket.properties.WebSocketProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mineguard.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
public class WebSocketAutoConfiguration implements WebSocketConfigurer {

    private final WebSocketProperties webSocketProperties;

    public WebSocketAutoConfiguration(WebSocketProperties webSocketProperties) {
        this.webSocketProperties = webSocketProperties;
    }

    @Bean
    public WebSocketHandler webSocketHandler(
            com.klzw.common.websocket.manager.ConnectionManager connectionManager,
            com.klzw.common.websocket.manager.OnlineUserManager onlineUserManager,
            com.klzw.common.websocket.manager.MessageManager messageManager,
            com.klzw.common.auth.util.JwtUtils jwtUtils,
            com.klzw.common.websocket.service.MessageHistoryService messageHistoryService,
            com.klzw.common.websocket.service.SmartMessagePushService smartMessagePushService) {
        return new WebSocketHandler(
            connectionManager,
            onlineUserManager,
            messageManager,
            jwtUtils,
            webSocketProperties,
            messageHistoryService,
            smartMessagePushService
        );
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = webSocketProperties.getAllowedOrigins();

        registry.addHandler(webSocketHandler(null, null, null, null, null, null), "/ws")
                .addHandler(webSocketHandler(null, null, null, null, null, null), "/ws/message")
                .addHandler(webSocketHandler(null, null, null, null, null, null), "/ws/vehicle")
                .addHandler(webSocketHandler(null, null, null, null, null, null), "/ws/warning")
                .addHandler(webSocketHandler(null, null, null, null, null, null), "/ws/trip")
                .addHandler(webSocketHandler(null, null, null, null, null, null), "/ws/system")
                .setAllowedOrigins(allowedOrigins != null ? allowedOrigins : new String[0]);
    }
}
