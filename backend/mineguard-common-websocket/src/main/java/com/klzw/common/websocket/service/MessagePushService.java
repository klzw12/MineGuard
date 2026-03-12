package com.klzw.common.websocket.service;

import com.klzw.common.websocket.domain.Message;

import java.util.List;
import java.util.Map;

public interface MessagePushService {
    void pushToUser(String userId, Message message);
    
    void broadcast(Message message);
    
    void multicast(List<String> userIds, Message message);
    
    void pushToRole(String role, Message message);
    
    void pushToTopic(String topic, Message message);
    
    void pushVehicleStatus(String userId, Long carId, Map<String, Object> vehicleData);
    
    void pushWarningNotification(String userId, Long warningId, Map<String, Object> warningData);
    
    void pushDispatchCommand(String userId, Long commandId, Map<String, Object> commandData);
    
    void pushTripUpdate(String userId, Long tripId, Map<String, Object> tripData);
    
    void pushSystemNotice(String title, String content, String type);
}
