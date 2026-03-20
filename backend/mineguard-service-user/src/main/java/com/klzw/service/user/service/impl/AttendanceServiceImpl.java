package com.klzw.service.user.service.impl;

import com.klzw.service.user.config.AttendanceProperties;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.DriverAttendance;
import com.klzw.service.user.enums.AttendanceStatusEnum;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.mapper.DriverAttendanceMapper;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.service.AttendanceService;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final DriverAttendanceMapper attendanceMapper;
    private final DriverMapper driverMapper;
    private final AttendanceProperties attendanceProperties;

    @Override
    @Transactional
    public AttendanceVO checkIn(CheckInDTO dto) {
        Long userId = dto.getUserId();
        log.info("司机上班打卡，用户ID：{}", userId);

        // 验证司机是否存在
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "司机不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查询今天是否已有出勤记录
        DriverAttendance attendance = attendanceMapper.selectByDriverIdAndDate(driver.getId(), today);

        if (attendance == null) {
            // 创建新的出勤记录
            attendance = new DriverAttendance();
            // 使用雪花算法生成Long类型的id
            attendance.setDriverId(driver.getId());
            attendance.setAttendanceDate(today);
            attendance.setCheckInTime(now);
            attendance.setCreateTime(now);

            // 判断迟到状态
            LocalDateTime workStart = LocalDateTime.of(today, attendanceProperties.getWorkStartTime());
            if (now.isAfter(workStart.plusMinutes(attendanceProperties.getLateThreshold()))) {
                // 迟到超过阈值
                attendance.setStatus(AttendanceStatusEnum.LATE.getValue());
                attendance.setLateMinutes((int) ChronoUnit.MINUTES.between(workStart, now));
            } else {
                attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());
            }

            attendance.setRemark(dto.getRemark());
            attendanceMapper.insert(attendance);
        } else {
            // 更新上班时间（重复打卡）
            attendance.setCheckInTime(now);
            
            // 重新判断迟到状态
            LocalDateTime workStart = LocalDateTime.of(today, attendanceProperties.getWorkStartTime());
            if (now.isAfter(workStart.plusMinutes(attendanceProperties.getLateThreshold()))) {
                attendance.setStatus(AttendanceStatusEnum.LATE.getValue());
                attendance.setLateMinutes((int) ChronoUnit.MINUTES.between(workStart, now));
            } else {
                attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());
                attendance.setLateMinutes(0);
            }

            if (dto.getRemark() != null) {
                attendance.setRemark(dto.getRemark());
            }
            attendanceMapper.updateById(attendance);
        }

        log.info("司机上班打卡成功，用户ID：{}，司机ID：{}，状态：{}", userId, driver.getId(), attendance.getStatus());
        return convertToVO(attendance, driver.getDriverName());
    }

    @Override
    @Transactional
    public AttendanceVO checkOut(CheckOutDTO dto) {
        Long userId = dto.getUserId();
        log.info("司机下班打卡，用户ID：{}", userId);

        // 验证司机是否存在
        Driver driver = driverMapper.selectByUserId(String.valueOf(userId));
        if (driver == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "司机不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查询今天的出勤记录
        DriverAttendance attendance = attendanceMapper.selectByDriverIdAndDate(driver.getId(), today);

        if (attendance == null) {
            // 没有上班记录，创建新的出勤记录（异常打卡）
            attendance = new DriverAttendance();
            // 使用雪花算法生成Long类型的id
            attendance.setDriverId(driver.getId());
            attendance.setAttendanceDate(today);
            attendance.setCheckOutTime(now);
            attendance.setStatus(AttendanceStatusEnum.ABSENT.getValue());
            attendance.setRemark("未打上班卡，直接打下班卡");
            attendance.setCreateTime(now);
            attendanceMapper.insert(attendance);
        } else {
            // 更新下班时间
            attendance.setCheckOutTime(now);

            // 判断早退状态
            LocalDateTime workEnd = LocalDateTime.of(today, attendanceProperties.getWorkEndTime());
            if (now.isBefore(workEnd.minusMinutes(attendanceProperties.getEarlyLeaveThreshold()))) {
                // 早退超过阈值
                attendance.setEarlyLeaveMinutes((int) ChronoUnit.MINUTES.between(now, workEnd));
                // 如果之前是正常状态，改为早退
                if (attendance.getStatus() == AttendanceStatusEnum.NORMAL.getValue()) {
                    attendance.setStatus(AttendanceStatusEnum.EARLY_LEAVE.getValue());
                }
            }

            if (dto.getRemark() != null) {
                attendance.setRemark(dto.getRemark());
            }
            attendanceMapper.updateById(attendance);
        }

        log.info("司机下班打卡成功，用户ID：{}，司机ID：{}，状态：{}", userId, driver.getId(), attendance.getStatus());
        return convertToVO(attendance, driver.getDriverName());
    }

    @Override
    public AttendanceVO getAttendanceByDate(Long driverId, LocalDate date) {
        DriverAttendance attendance = attendanceMapper.selectByDriverIdAndDate(driverId, date);
        if (attendance == null) {
            return null;
        }

        Driver driver = driverMapper.selectById(driverId);
        String driverName = driver != null ? driver.getDriverName() : null;

        return convertToVO(attendance, driverName);
    }

    @Override
    public List<AttendanceVO> getAttendanceListByMonth(Long driverId, String yearMonth) {
        // 解析年月
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<DriverAttendance> list = attendanceMapper.selectByDriverIdAndMonth(driverId, startDate, endDate);

        Driver driver = driverMapper.selectById(driverId);
        String driverName = driver != null ? driver.getDriverName() : null;

        return list.stream()
                .map(attendance -> convertToVO(attendance, driverName))
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceStatisticsVO getAttendanceStatistics(Long driverId, String yearMonth) {
        // 解析年月
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 应出勤天数（工作日）
        int shouldAttendanceDays = calculateWorkDays(startDate, endDate);

        // 实际出勤天数
        Integer actualAttendanceDays = attendanceMapper.countAttendanceDays(driverId, startDate, endDate);
        if (actualAttendanceDays == null) {
            actualAttendanceDays = 0;
        }

        // 迟到次数
        Integer lateTimes = attendanceMapper.countLateTimes(driverId, startDate, endDate);
        if (lateTimes == null) {
            lateTimes = 0;
        }

        // 早退次数
        Integer earlyLeaveTimes = attendanceMapper.countEarlyLeaveTimes(driverId, startDate, endDate);
        if (earlyLeaveTimes == null) {
            earlyLeaveTimes = 0;
        }

        // 缺勤天数 = 应出勤天数 - 实际出勤天数
        int absentDays = shouldAttendanceDays - actualAttendanceDays;
        if (absentDays < 0) {
            absentDays = 0;
        }

        // 正常出勤天数 = 实际出勤天数 - 迟到次数 - 早退次数
        int normalDays = actualAttendanceDays - lateTimes - earlyLeaveTimes;
        if (normalDays < 0) {
            normalDays = 0;
        }

        // 出勤率
        double attendanceRate = shouldAttendanceDays > 0 
                ? (double) actualAttendanceDays / shouldAttendanceDays * 100 
                : 0;

        Driver driver = driverMapper.selectById(driverId);

        AttendanceStatisticsVO statistics = new AttendanceStatisticsVO();
        statistics.setDriverId(driverId != null ? driverId.toString() : null);
        statistics.setDriverName(driver != null ? driver.getDriverName() : null);
        statistics.setMonth(yearMonth);
        statistics.setShouldAttendanceDays(shouldAttendanceDays);
        statistics.setActualAttendanceDays(actualAttendanceDays);
        statistics.setNormalDays(normalDays);
        statistics.setLateTimes(lateTimes);
        statistics.setEarlyLeaveTimes(earlyLeaveTimes);
        statistics.setAbsentDays(absentDays);
        statistics.setAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);

        return statistics;
    }

    @Override
    @Transactional
    public AttendanceVO supplementAttendance(Long attendanceId, LocalDate checkInTime, LocalDate checkOutTime, Integer status, String remark) {
        DriverAttendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "出勤记录不存在");
        }

        if (checkInTime != null) {
            attendance.setCheckInTime(checkInTime.atStartOfDay());
        }
        if (checkOutTime != null) {
            attendance.setCheckOutTime(checkOutTime.atStartOfDay());
        }
        if (status != null) {
            attendance.setStatus(status);
        }
        if (remark != null) {
            attendance.setRemark(remark);
        }

        attendanceMapper.updateById(attendance);

        Driver driver = driverMapper.selectById(attendance.getDriverId());
        String driverName = driver != null ? driver.getDriverName() : null;

        log.info("补卡成功，出勤记录ID：{}", attendanceId);
        return convertToVO(attendance, driverName);
    }

    /**
     * 转换为 VO
     */
    private AttendanceVO convertToVO(DriverAttendance attendance, String driverName) {
        AttendanceVO vo = new AttendanceVO();
        vo.setId(attendance.getId() != null ? attendance.getId().toString() : null);
        vo.setDriverId(attendance.getDriverId() != null ? attendance.getDriverId().toString() : null);
        vo.setDriverName(driverName);
        vo.setAttendanceDate(attendance.getAttendanceDate());
        vo.setCheckInTime(attendance.getCheckInTime());
        vo.setCheckOutTime(attendance.getCheckOutTime());
        vo.setStatus(attendance.getStatus());
        vo.setLateMinutes(attendance.getLateMinutes());
        vo.setEarlyLeaveMinutes(attendance.getEarlyLeaveMinutes());
        vo.setRemark(attendance.getRemark());
        vo.setCreateTime(attendance.getCreateTime());

        AttendanceStatusEnum statusEnum = AttendanceStatusEnum.getByValue(attendance.getStatus());
        if (statusEnum != null) {
            vo.setStatusLabel(statusEnum.getLabel());
        }

        return vo;
    }

    /**
     * 计算工作日天数（简化版，不考虑节假日）
     */
    private int calculateWorkDays(LocalDate startDate, LocalDate endDate) {
        int workDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            // 周一到周五为工作日
            if (date.getDayOfWeek().getValue() <= 5) {
                workDays++;
            }
            date = date.plusDays(1);
        }
        return workDays;
    }
}
