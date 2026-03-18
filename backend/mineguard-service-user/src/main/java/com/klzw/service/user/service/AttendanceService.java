package com.klzw.service.user.service;

import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤服务接口
 */
public interface AttendanceService {

    /**
     * 上班打卡
     *
     * @param dto 打卡信息
     * @return 出勤记录
     */
    AttendanceVO checkIn(CheckInDTO dto);

    /**
     * 下班打卡
     *
     * @param dto 打卡信息
     * @return 出勤记录
     */
    AttendanceVO checkOut(CheckOutDTO dto);

    /**
     * 获取司机某天的出勤记录
     *
     * @param driverId 司机ID
     * @param date 日期
     * @return 出勤记录
     */
    AttendanceVO getAttendanceByDate(Long driverId, LocalDate date);

    /**
     * 获取司机某月的出勤记录列表
     *
     * @param driverId 司机ID
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 出勤记录列表
     */
    List<AttendanceVO> getAttendanceListByMonth(Long driverId, String yearMonth);

    /**
     * 获取司机某月的出勤统计
     *
     * @param driverId 司机ID
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 出勤统计
     */
    AttendanceStatisticsVO getAttendanceStatistics(Long driverId, String yearMonth);

    /**
     * 补卡（管理员功能）
     *
     * @param attendanceId 出勤记录ID
     * @param checkInTime 上班时间
     * @param checkOutTime 下班时间
     * @param status 状态
     * @param remark 备注
     * @return 出勤记录
     */
    AttendanceVO supplementAttendance(Long attendanceId, LocalDate checkInTime, LocalDate checkOutTime, Integer status, String remark);
}
