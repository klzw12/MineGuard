package com.klzw.service.trip.config;

import com.klzw.service.trip.websocket.TripWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Slf4j
public class TripWebSocketConfig implements WebSocketConfigurer {

    private final TripWebSocketHandler tripWebSocketHandler;

    public TripWebSocketConfig(TripWebSocketHandler tripWebSocketHandler) {
        this.tripWebSocketHandler = tripWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册ws连接");
        registry.addHandler(tripWebSocketHandler, "/ws/trip")
                .setAllowedOrigins("*");
    }
}
