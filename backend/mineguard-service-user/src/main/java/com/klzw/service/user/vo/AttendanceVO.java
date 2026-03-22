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

    private String userId;

    private String userName;

    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Double checkInLatitude;

    private Double checkInLongitude;

    private String checkInAddress;

    private Double checkOutLatitude;

    private Double checkOutLongitude;

    private String checkOutAddress;

    private Integer status;

    private String statusLabel;

    private Integer lateMinutes;

    private Integer earlyLeaveMinutes;

    private String remark;
}
