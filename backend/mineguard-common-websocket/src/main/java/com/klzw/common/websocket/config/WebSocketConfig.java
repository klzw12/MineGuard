package com.klzw.common.websocket.config;

import com.klzw.common.websocket.handler.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;
    private final WebSocketProperties webSocketProperties;

    public WebSocketConfig(WebSocketHandler webSocketHandler, WebSocketProperties webSocketProperties) {
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
