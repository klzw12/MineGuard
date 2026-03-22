package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_attendance")
public class UserAttendance extends BaseEntity {

    private Long userId;

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

    private Integer lateMinutes;

    private Integer earlyLeaveMinutes;

    private Integer leaveType;

    private LocalDateTime leaveStartTime;

    private LocalDateTime leaveEndTime;

    private String remark;
}
