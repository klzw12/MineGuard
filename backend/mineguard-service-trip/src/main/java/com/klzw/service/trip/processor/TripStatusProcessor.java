package com.klzw.service.trip.processor;

import com.klzw.common.core.client.MessageClient;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripStatusProcessor {

    private final TripService tripService;
    private final MessageClient messageClient;

    public void processStatusChange(Trip trip, int oldStatus, int newStatus) {
        log.info("行程状态变化：行程ID={}, 旧状态={}, 新状态={}", 
                trip.getId(), oldStatus, newStatus);

        String notificationContent = generateNotificationContent(trip, oldStatus, newStatus);
        if (notificationContent != null) {
            messageClient.sendNotification(
                trip.getDriverId(),
                "行程状态变更",
                notificationContent,
                3,
                trip.getId(),
                "TRIP"
            );

            messageClient.sendNotification(
                1L,
                "行程状态变更",
                notificationContent,
                3,
                trip.getId(),
                "TRIP"
            );
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
