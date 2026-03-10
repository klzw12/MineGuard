package com.klzw.common.websocket.config;

import com.klzw.common.websocket.handler.WebSocketHandler;
import com.klzw.common.websocket.properties.WebSocketProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mineguard.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
public class WebSocketAutoConfiguration implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final WebSocketProperties webSocketProperties;

    public WebSocketAutoConfiguration(WebSocketHandler webSocketHandler, WebSocketProperties webSocketProperties) {
        this.webSocketHandler = webSocketHandler;
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = webSocketProperties.getAllowedOrigins();

        registry.addHandler(webSocketHandler, "/ws")
                .addHandler(webSocketHandler, "/ws/message")
                .addHandler(webSocketHandler, "/ws/vehicle")
                .addHandler(webSocketHandler, "/ws/warning")
                .addHandler(webSocketHandler, "/ws/trip")
                .addHandler(webSocketHandler, "/ws/system")
                .setAllowedOrigins(allowedOrigins != null ? allowedOrigins : new String[0]);
    }
}
