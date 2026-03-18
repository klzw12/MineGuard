package com.klzw.service.user.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出勤记录 VO
 */
@Data
public class AttendanceVO {

    private String id;

    private String driverId;

    private String driverName;

    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Integer status;

    private String statusLabel;

    private Integer lateMinutes;

    private Integer earlyLeaveMinutes;

    private String remark;

    private LocalDateTime createTime;
}
