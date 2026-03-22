package com.klzw.service.vehicle.service.impl;

import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.service.FatigueWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FatigueWarningServiceImpl implements FatigueWarningService {

    private final VehicleMapper vehicleMapper;
    private final MessageClient messageClient;
    private final TripClient tripClient;
    private final RestTemplate restTemplate;

    @Value("${service.warning.url:http://localhost:8087}")
    private String warningServiceUrl;

    @Override
    public void sendFatigueWarning(Long vehicleId, Long tripId, int drivingMinutes) {
        log.info("发送疲劳驾驶预警: vehicleId={}, tripId={}, drivingMinutes={}", 
                vehicleId, tripId, drivingMinutes);
        
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            log.error("车辆不存在: vehicleId={}", vehicleId);
            return;
        }
        
        Long driverId = null;
        try {
            TripResponse trip = tripClient.getLatestTrip(vehicleId).block();
            if (trip != null) {
                driverId = trip.getDriverId();
            }
        } catch (Exception e) {
            log.warn("获取行程司机信息失败: vehicleId={}", vehicleId);
        }
        
        if (driverId == null) {
            log.error("无法获取司机ID: vehicleId={}", vehicleId);
            return;
        }
        
        String vehicleNo = vehicle.getVehicleNo();
        String content = String.format("车辆 %s 已连续驾驶 %d 分钟，超过4小时，请立即休息！", 
                vehicleNo, drivingMinutes);
        
        try {
            Map<String, Object> warningRequest = new HashMap<>();
            warningRequest.put("vehicleId", vehicleId);
            warningRequest.put("tripId", tripId);
            warningRequest.put("warningType", 7);
            warningRequest.put("warningLevel", 2);
            warningRequest.put("warningContent", content);
            warningRequest.put("vehicleNo", vehicleNo);
            
            restTemplate.postForEntity(
                warningServiceUrl + "/api/warning/create",
                warningRequest,
                Void.class
            );
            
            log.info("疲劳驾驶预警已创建: vehicleId={}", vehicleId);
        } catch (Exception e) {
            log.error("创建疲劳驾驶预警失败: vehicleId={}", vehicleId, e);
        }
        
        try {
            messageClient.sendNotification(
                driverId,
                "疲劳驾驶预警",
                content,
                2,
                tripId,
                "TRIP"
            );
            
            messageClient.sendMessageByRole(
                "ADMIN",
                "疲劳驾驶预警",
                content,
                2
            );
            
            log.info("疲劳驾驶预警通知已发送: vehicleId={}", vehicleId);
        } catch (Exception e) {
            log.error("发送疲劳驾驶预警通知失败: vehicleId={}", vehicleId, e);
        }
    }
}
