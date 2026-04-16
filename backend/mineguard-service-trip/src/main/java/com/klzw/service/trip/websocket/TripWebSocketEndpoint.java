package com.klzw.service.trip.websocket;

import com.klzw.common.core.domain.dto.TripTrackDTO;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.vo.TripVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Scope("prototype")
@ServerEndpoint("/ws/trip")
public class TripWebSocketEndpoint {

    private static TripService tripService;
    private static TripTrackService tripTrackService;
    private static final ConcurrentHashMap<Session, Long> sessionTripMap = new ConcurrentHashMap<>();

    public static void setTripService(TripService tripService) {
        TripWebSocketEndpoint.tripService = tripService;
    }

    public static void setTripTrackService(TripTrackService tripTrackService) {
        TripWebSocketEndpoint.tripTrackService = tripTrackService;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("TripWebSocketEndpoint连接建立: sessionId={}", 
                session.getId());
        
        // 从查询参数中获取tripId
        Long tripId = getTripIdFromSession(session);
        if (tripId == null) {
            sendErrorMessage(session, "TRIP_ID_MISSING", "行程ID缺失");
            return;
        }
        
        // 验证行程状态
        try {
            TripVO trip = tripService.getById(tripId);
            if (trip == null) {
                sendErrorMessage(session, "TRIP_NOT_FOUND", "行程不存在");
                return;
            }
            
            if (trip.getStatus() != TripStatusEnum.IN_PROGRESS.getCode()) {
                sendErrorMessage(session, "TRIP_STATUS_ERROR", "只有进行中的行程可以建立WebSocket连接");
                return;
            }
            
            // 连接建立成功
            sessionTripMap.put(session, tripId);
            log.info("行程WebSocket连接建立成功: tripId={}, sessionId={}", tripId, session.getId());
        } catch (Exception e) {
            log.error("行程WebSocket连接失败: tripId={}", getTripIdFromSession(session), e);
            sendErrorMessage(session, "CONNECTION_FAILED", "连接失败: " + e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            Long tripId = sessionTripMap.get(session);
            if (tripId == null) {
                log.warn("WebSocket 消息处理失败：tripId 为空");
                return;
            }
            
            // 解析前端发送的轨迹数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(message);
            
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
            // 从 JSON 中提取司机ID
            if (jsonNode.has("driverId")) {
                try {
                    trackDTO.setDriverId(jsonNode.get("driverId").asLong());
                } catch (NumberFormatException e) {
                    log.warn("司机ID格式错误：{}", jsonNode.get("driverId").asText());
                }
            }
            
            // 从行程中获取 vehicleId 和 driverId
            TripVO trip = tripService.getById(tripId);
            if (trip != null) {
                if (trip.getVehicleId() != null) {
                    try {
                        // 尝试将 String 类型的 vehicleId 转换为 Long
                        trackDTO.setVehicleId(Long.parseLong(trip.getVehicleId()));
                    } catch (NumberFormatException e) {
                        log.warn("车辆ID格式错误：{}", trip.getVehicleId());
                        // 不设置 vehicleId，避免影响轨迹点存储
                    }
                }
                if (trip.getDriverId() != null && trackDTO.getDriverId() == null) {
                    try {
                        // 尝试将 String 类型的 driverId 转换为 Long
                        trackDTO.setDriverId(Long.parseLong(trip.getDriverId()));
                    } catch (NumberFormatException e) {
                        log.warn("司机ID格式错误：{}", trip.getDriverId());
                    }
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

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        Long tripId = sessionTripMap.remove(session);
        log.info("TripWebSocketEndpoint连接关闭: sessionId={}, tripId={}, reason={}", 
                session.getId(), tripId, closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        Long tripId = sessionTripMap.get(session);
        log.error("TripWebSocketEndpoint错误: sessionId={}, tripId={}", session.getId(), tripId, error);
    }

    private Long getTripIdFromSession(Session session) {
        String query = session.getQueryString();
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

    private void sendErrorMessage(Session session, String errorCode, String errorMessage) {
        try {
            String message = String.format("{\"messageType\": \"AUTH\", \"content\": {\"status\": \"FAILED\", \"errorCode\": \"%s\", \"errorMessage\": \"%s\"}}", errorCode, errorMessage);
            session.getBasicRemote().sendText(message);
            session.close();
        } catch (IOException e) {
            log.error("发送连接失败消息失败", e);
        }
    }
}