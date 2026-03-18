package com.klzw.service.trip.processor;

import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.entity.TripNotification;
import com.klzw.service.trip.service.TripNotificationService;
import com.klzw.service.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripStatusProcessor {

    private final TripService tripService;
    private final TripNotificationService tripNotificationService;

    /**
     * 处理行程状态变化
     * @param trip 行程对象
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public void processStatusChange(Trip trip, int oldStatus, int newStatus) {
        log.info("行程状态变化：行程ID={}, 旧状态={}, 新状态={}", 
                trip.getId(), oldStatus, newStatus);

        // 生成状态变化通知
        String notificationContent = generateNotificationContent(trip, oldStatus, newStatus);
        if (notificationContent != null) {
            // 发送给司机
            TripNotification driverNotification = new TripNotification();
            driverNotification.setTripId(trip.getId());
            driverNotification.setUserId(trip.getDriverId());
            driverNotification.setNotificationType("trip_status_change");
            driverNotification.setNotificationContent(notificationContent);
            tripNotificationService.createNotification(driverNotification);

            // 发送给管理员（假设管理员ID为1）
            TripNotification adminNotification = new TripNotification();
            adminNotification.setTripId(trip.getId());
            adminNotification.setUserId(1L); // 管理员ID
            adminNotification.setNotificationType("trip_status_change");
            adminNotification.setNotificationContent(notificationContent);
            tripNotificationService.createNotification(adminNotification);
        }
    }

    /**
     * 生成状态变化通知内容
     * @param trip 行程对象
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 通知内容
     */
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
