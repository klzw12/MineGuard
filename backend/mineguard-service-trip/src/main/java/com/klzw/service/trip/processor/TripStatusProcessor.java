package com.klzw.service.trip.processor;

import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.enums.VehicleStatusEnum;
import com.klzw.service.trip.entity.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripStatusProcessor {

    private final MessageClient messageClient;
    private final VehicleClient vehicleClient;

    public void processStatusChange(Trip trip, int oldStatus, int newStatus) {
        log.info("行程状态变化：行程ID={}, 旧状态={}, 新状态={}", 
                trip.getId(), oldStatus, newStatus);

        updateVehicleStatus(trip, newStatus);
        
        if (trip.getEndLongitude() != null && trip.getEndLatitude() != null) {
            vehicleClient.updateStatusWithLocation(trip.getVehicleId(), newStatus, trip.getEndLatitude(), trip.getEndLongitude());
        } else {
            log.warn("行程结束经纬度为空，无法更新车辆状态表的经纬度");
        }
        
        String notificationContent = generateNotificationContent(trip, oldStatus, newStatus);
        if (notificationContent != null) {
            messageClient.sendMessage(
                trip.getDriverId(),
                "行程状态变更",
                notificationContent,
                "TRIP_STATUS_CHANGE",
                trip.getId().toString()
            );
        }
    }

    private void updateVehicleStatus(Trip trip, int newStatus) {
        try {
            Long vehicleId = trip.getVehicleId();
            if (vehicleId == null) {
                log.warn("行程车辆ID为空，无法更新车辆状态：行程ID={}", trip.getId());
                return;
            }

            int vehicleStatus;
            switch (newStatus) {
                case 0:
                case 1:
                    vehicleStatus = VehicleStatusEnum.IDLE.getCode();
                    break;
                case 2:
                    vehicleStatus = VehicleStatusEnum.RUNNING.getCode();
                    break;
                case 3:
                case 4:
                    vehicleStatus = VehicleStatusEnum.IDLE.getCode();
                    break;
                case 5:
                    vehicleStatus = VehicleStatusEnum.RUNNING.getCode();
                    break;
                default:
                    vehicleStatus = VehicleStatusEnum.IDLE.getCode();
            }

            log.info("更新车辆状态：vehicleId={}, status={}", vehicleId, vehicleStatus);
            com.klzw.common.core.domain.dto.VehicleStatus status = new com.klzw.common.core.domain.dto.VehicleStatus();
            status.setStatus(vehicleStatus);
            vehicleClient.updateStatus(vehicleId, status);
        } catch (Exception e) {
            log.error("更新车辆状态失败：行程ID={}", trip.getId(), e);
        }
    }

    private String generateNotificationContent(Trip trip, int oldStatus, int newStatus) {
        String tripNo = trip.getTripNo();
        
        switch (newStatus) {
            case 0:
                return "行程 " + tripNo + " 已创建，等待司机接单";
            case 1:
                return "行程 " + tripNo + " 已被司机接单";
            case 2:
                return "行程 " + tripNo + " 已开始";
            case 3:
                return "行程 " + tripNo + " 已完成";
            case 4:
                return "行程 " + tripNo + " 已取消";
            case 5:
                return "行程 " + tripNo + " 已暂停";
            default:
                return null;
        }
    }
}
