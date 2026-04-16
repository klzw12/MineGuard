package com.klzw.service.trip.config;

import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.websocket.TripWebSocketEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEndpointConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final TripService tripService;
    private final TripTrackService tripTrackService;

    @Autowired
    public WebSocketEndpointConfig(TripService tripService, TripTrackService tripTrackService) {
        this.tripService = tripService;
        this.tripTrackService = tripTrackService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 手动注入服务到 WebSocket 端点
        TripWebSocketEndpoint.setTripService(tripService);
        TripWebSocketEndpoint.setTripTrackService(tripTrackService);
    }
}