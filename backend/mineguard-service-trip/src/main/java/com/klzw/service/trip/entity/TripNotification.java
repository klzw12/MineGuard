package com.klzw.service.trip.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("trip_notification")
public class TripNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tripId;

    private Long userId;

    private String notificationType;

    private String notificationContent;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
