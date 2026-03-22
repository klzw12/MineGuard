package com.klzw.service.user.vo;

import lombok.Data;

/**
 * 出勤统计 VO
 */
@Data
public class AttendanceStatisticsVO {

    private String userId;

    private String userName;

    private String month;

    private Integer shouldAttendanceDays;

    private Integer actualAttendanceDays;

    private Integer normalDays;

    private Integer lateTimes;

    private Integer earlyLeaveTimes;

    private Integer absentDays;

    private Double attendanceRate;
}
