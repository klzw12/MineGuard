package com.klzw.service.trip.websocket;

import com.klzw.common.websocket.handler.WebSocketHandler;
import com.klzw.common.websocket.manager.ConnectionManager;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.websocket.properties.WebSocketProperties;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.SmartMessagePushService;
import com.klzw.service.trip.dto.TripTrackDTO;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.vo.TripVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

@Slf4j
@Component("tripWebSocketHandler")
public class TripWebSocketHandler extends WebSocketHandler {

    private final TripTrackService tripTrackService;
    private final TripService tripService;
    private final RestClient restClient = RestClient.create();

    private static final String WARNING_SERVICE_URL = "http://mineguard-service-warning:8080/api/warning/process-event";

    public TripWebSocketHandler(ConnectionManager connectionManager, 
                           OnlineUserManager onlineUserManager,
                           MessageManager messageManager,
                           JwtUtils jwtUtils,
                           WebSocketProperties webSocketProperties,
                           MessageHistoryService messageHistoryService,
                           SmartMessagePushService smartMessagePushService,
                           TripTrackService tripTrackService,
                           TripService tripService) {
        super(connectionManager, onlineUserManager, messageManager, jwtUtils, webSocketProperties, messageHistoryService, smartMessagePushService);
        this.tripTrackService = tripTrackService;
        this.tripService = tripService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("TripWebSocket连接建立: sessionId={}, remoteAddress={}", 
                session.getId(), session.getRemoteAddress());
        
        // 从URL参数中获取tripId
        Long tripId = getTripIdFromSession(session);
        if (tripId == null) {
            sendConnectionFailedMessage(session, "TRIP_ID_MISSING", "行程ID缺失");
            return;
        }
        
        // 验证行程状态
        try {
            TripVO trip = tripService.getById(tripId);
            if (trip == null) {
                sendConnectionFailedMessage(session, "TRIP_NOT_FOUND", "行程不存在");
                return;
            }
            
            if (trip.getStatus() != TripStatusEnum.IN_PROGRESS.getCode()) {
                sendConnectionFailedMessage(session, "TRIP_STATUS_ERROR", "只有进行中的行程可以建立WebSocket连接");
                return;
            }
            
            // 调用父类方法完成认证
            super.afterConnectionEstablished(session);
            
            log.info("行程WebSocket连接建立成功: tripId={}, sessionId={}", tripId, session.getId());
        } catch (Exception e) {
            log.error("行程WebSocket连接失败: tripId={}", tripId, e);
            sendConnectionFailedMessage(session, "CONNECTION_FAILED", "连接失败: " + e.getMessage());
        }
    }
    
    private void sendConnectionFailedMessage(WebSocketSession session, String errorCode, String errorMessage) {
        try {
            String message = String.format("{\"messageType\": \"AUTH\", \"content\": {\"status\": \"FAILED\", \"errorCode\": \"%s\", \"errorMessage\": \"%s\"}}", errorCode, errorMessage);
            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
            session.close();
        } catch (Exception e) {
            log.error("发送连接失败消息失败", e);
        }
    }

    private Long getTripIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("tripId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("tripId=")) {
                    try {
                        return Long.parseLong(param.substring(7));
                    } catch (NumberFormatException e) {
                        log.warn("无效的tripId参数: {}", param.substring(7));
                    }
                }
            }
        }
        return null;
    }


}
