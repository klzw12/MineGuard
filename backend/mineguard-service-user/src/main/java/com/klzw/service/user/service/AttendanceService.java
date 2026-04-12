package com.klzw.service.user.service;

import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * 获取用户某天的出勤记录
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 出勤记录
     */
    AttendanceVO getAttendanceByDate(Long userId, LocalDate date);

    /**
     * 获取用户某月的出勤记录列表
     *
     * @param userId 用户ID
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 出勤记录列表
     */
    List<AttendanceVO> getAttendanceListByMonth(Long userId, String yearMonth);

    /**
     * 获取用户某月的出勤统计
     *
     * @param userId 用户ID
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 出勤统计
     */
    AttendanceStatisticsVO getAttendanceStatistics(Long userId, String yearMonth);

    /**
     * 获取用户某日期范围的出勤统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 出勤统计
     */
    AttendanceStatisticsVO getAttendanceStatisticsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

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
    AttendanceVO supplementAttendance(Long attendanceId, LocalDateTime checkInTime, LocalDateTime checkOutTime, Integer status, String remark);

    /**
     * 请假申请
     *
     * @param userId 用户ID
     * @param leaveType 请假类型：1-事假 2-病假 3-年假 4-调休
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reason 请假原因
     * @return 出勤记录
     */
    AttendanceVO applyLeave(Long userId, Integer leaveType, LocalDateTime startTime, LocalDateTime endTime, String reason);

    /**
     * 取消请假
     *
     * @param attendanceId 出勤记录ID
     * @return 是否成功
     */
    boolean cancelLeave(Long attendanceId);

    /**
     * 获取请假记录列表
     *
     * @param userId 用户ID
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 请假记录列表
     */
    List<AttendanceVO> getLeaveList(Long userId, String yearMonth);

    /**
     * 获取可用司机ID列表（排除请假中的司机）
     *
     * @return 可用司机ID列表
     */
    List<Long> getAvailableDriverIds();
}
