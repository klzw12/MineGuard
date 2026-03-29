package com.klzw.service.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 考勤配置属性
 * <p>
 * 用于配置上下班时间、迟到早退阈值等考勤相关参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "mineguard.attendance")
public class AttendanceProperties {

    /**
     * 上班时间（默认 9:00）
     */
    private LocalTime workStartTime = LocalTime.of(9, 0);

    /**
     * 下班时间（默认 18:00）
     */
    private LocalTime workEndTime = LocalTime.of(18, 0);

    /**
     * 迟到阈值（分钟，默认 15 分钟）
     */
    private int lateThreshold = 15;

    /**
     * 早退阈值（分钟，默认 15 分钟）
     */
    private int earlyLeaveThreshold = 15;
}
