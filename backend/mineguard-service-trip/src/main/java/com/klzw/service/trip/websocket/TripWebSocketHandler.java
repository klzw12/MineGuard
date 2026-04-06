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
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Component("tripWebSocketHandler")
public class TripWebSocketHandler extends WebSocketHandler {

    private final TripTrackService tripTrackService;
    private final TripService tripService;

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
                        log.warn("无效的 tripId 参数：{}", param.substring(7));
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Long tripId = getTripIdFromSession(session);
            if (tripId == null) {
                log.warn("WebSocket 消息处理失败：tripId 为空");
                return;
            }
            
            // 解析前端发送的轨迹数据
            String payload = message.getPayload();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // 构建 TripTrackDTO
            TripTrackDTO trackDTO = new TripTrackDTO();
            trackDTO.setTripId(tripId);
            
            // 从 JSON 中提取数据
            if (jsonNode.has("longitude")) {
                trackDTO.setLongitude(jsonNode.get("longitude").asDouble());
            }
            if (jsonNode.has("latitude")) {
                trackDTO.setLatitude(jsonNode.get("latitude").asDouble());
            }
            if (jsonNode.has("speed")) {
                trackDTO.setSpeed(jsonNode.get("speed").asDouble());
            }
            if (jsonNode.has("direction")) {
                trackDTO.setDirection(jsonNode.get("direction").asDouble());
            }
            if (jsonNode.has("altitude")) {
                trackDTO.setAltitude(jsonNode.get("altitude").asDouble());
            }
            if (jsonNode.has("recordTime")) {
                trackDTO.setRecordTime(jsonNode.get("recordTime").asLong());
            } else {
                // 如果没有时间戳，使用当前时间
                trackDTO.setRecordTime(System.currentTimeMillis());
            }
            
            // 从行程中获取 vehicleId
            TripVO trip = tripService.getById(tripId);
            if (trip != null && trip.getVehicleId() != null) {
                try {
                    // 尝试将 String 类型的 vehicleId 转换为 Long
                    trackDTO.setVehicleId(Long.parseLong(trip.getVehicleId()));
                } catch (NumberFormatException e) {
                    log.warn("车辆ID格式错误：{}", trip.getVehicleId());
                    // 不设置 vehicleId，避免影响轨迹点存储
                }
            }
            
            // 存储到 Redis（实时轨迹数据，供预警、vehicle 等模块读取）
            tripTrackService.uploadTrack(trackDTO);
            
            log.debug("WebSocket 轨迹数据接收成功：tripId={}, vehicleId={}, 位置=({}, {}), 速度={}", 
                tripId, trackDTO.getVehicleId(), trackDTO.getLongitude(), trackDTO.getLatitude(), trackDTO.getSpeed());
            
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
        }
    }
}
