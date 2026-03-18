package com.klzw.service.user.vo;

import lombok.Data;

/**
 * 出勤统计 VO
 */
@Data
public class AttendanceStatisticsVO {

    /**
     * 司机ID
     */
    private String driverId;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 统计月份
     */
    private String month;

    /**
     * 应出勤天数
     */
    private Integer shouldAttendanceDays;

    /**
     * 实际出勤天数
     */
    private Integer actualAttendanceDays;

    /**
     * 正常出勤天数
     */
    private Integer normalDays;

    /**
     * 迟到次数
     */
    private Integer lateTimes;

    /**
     * 早退次数
     */
    private Integer earlyLeaveTimes;

    /**
     * 缺勤天数
     */
    private Integer absentDays;

    /**
     * 出勤率
     */
    private Double attendanceRate;
}
