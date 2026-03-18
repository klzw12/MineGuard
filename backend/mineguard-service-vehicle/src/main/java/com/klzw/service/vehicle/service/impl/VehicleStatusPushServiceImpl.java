package com.klzw.service.vehicle.service.impl;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.service.SmartMessagePushService;
import com.klzw.service.vehicle.service.VehicleStatusPushService;
import com.klzw.service.vehicle.vo.VehicleStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VehicleStatusPushServiceImpl implements VehicleStatusPushService {

    @Resource
    private SmartMessagePushService smartMessagePushService;

    @Override
    public void pushVehicleStatus(Long vehicleId, Map<String, Object> status) {
        log.info("推送车辆状态: vehicleId={}, status={}", vehicleId, status);
        
        // 构建消息内容
        Map<String, Object> content = new HashMap<>();
        content.put("vehicleId", vehicleId);
        content.put("status", status);
        content.put("timestamp", System.currentTimeMillis());
        
        // 构建消息
        Message message = Message.builder()
                .messageId("VEHICLE_STATUS_" + System.currentTimeMillis() + "_" + vehicleId)
                .messageType(MessageTypeEnum.VEHICLE_STATUS)
                .sender("vehicle-service")
                .receiver("broadcast") // 广播给所有订阅了车辆状态的用户
                .content(content)
                .build();
        
        // 推送消息
        smartMessagePushService.pushToTopic("vehicle:" + vehicleId, message);
    }

    @Override
    public void pushStatusChange(Long vehicleId, VehicleStatusVO statusVO) {
        log.info("推送车辆状态变更: vehicleId={}, status={}", vehicleId, statusVO.getStatus());
        
        // 构建消息内容
        Map<String, Object> content = new HashMap<>();
        content.put("vehicleId", vehicleId);
        content.put("status", statusVO);
        content.put("timestamp", System.currentTimeMillis());
        
        // 构建消息
        Message message = Message.builder()
                .messageId("VEHICLE_STATUS_CHANGE_" + System.currentTimeMillis() + "_" + vehicleId)
                .messageType(MessageTypeEnum.VEHICLE_STATUS)
                .sender("vehicle-service")
                .receiver("broadcast") // 广播给所有订阅了车辆状态的用户
                .content(content)
                .build();
        
        // 推送消息
        smartMessagePushService.pushToTopic("vehicle:" + vehicleId, message);
    }

    @Override
    public void pushVehicleWarning(Long vehicleId, String warningType, String warningMessage) {
        log.info("推送车辆告警: vehicleId={}, type={}, message={}", vehicleId, warningType, warningMessage);
        
        // 构建消息内容
        Map<String, Object> content = new HashMap<>();
        content.put("vehicleId", vehicleId);
        content.put("warningType", warningType);
        content.put("warningMessage", warningMessage);
        content.put("timestamp", System.currentTimeMillis());
        
        // 构建消息
        Message message = Message.builder()
                .messageId("VEHICLE_WARNING_" + System.currentTimeMillis() + "_" + vehicleId)
                .messageType(MessageTypeEnum.WARNING_NOTIFICATION)
                .sender("vehicle-service")
                .receiver("broadcast")
                .content(content)
                .build();
        
        // 推送消息
        smartMessagePushService.pushToTopic("vehicle:" + vehicleId, message);
    }
}
