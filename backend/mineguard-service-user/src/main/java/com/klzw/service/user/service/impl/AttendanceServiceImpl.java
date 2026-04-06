package com.klzw.service.user.service.impl;

import com.klzw.common.core.client.DispatchClient;
import com.klzw.service.user.properties.AttendanceProperties;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.dto.LeaveApplyDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserAttendance;
import com.klzw.service.user.enums.AttendanceStatusEnum;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.common.core.exception.BaseException;
import com.klzw.common.core.enums.ResultCodeEnum;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.AttendanceService;
import com.klzw.service.user.service.UserService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final UserAttendanceMapper attendanceMapper;
    private final UserMapper userMapper;
    private final AttendanceProperties attendanceProperties;
    private final DriverMapper driverMapper;
    private final DispatchClient dispatchClient;
    private final UserService userService;

    @Override
    @Transactional
    public AttendanceVO checkIn(CheckInDTO dto) {
        Long userId = getCurrentUserId();
        log.info("用户上班打卡，用户ID：{}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "用户不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        UserAttendance attendance = attendanceMapper.selectByUserIdAndDate(userId, today);

        if (attendance == null) {
            attendance = new UserAttendance();
            attendance.setUserId(userId);
            attendance.setAttendanceDate(today);
            attendance.setCheckInTime(now);
            attendance.setCheckInLatitude(dto.getLatitude());
            attendance.setCheckInLongitude(dto.getLongitude());
            attendance.setCheckInAddress(dto.getAddress());
            attendance.setCreateTime(now);

            LocalDateTime workStart = LocalDateTime.of(today, attendanceProperties.getWorkStartTime());
            if (now.isAfter(workStart.plusMinutes(attendanceProperties.getLateThreshold()))) {
                attendance.setStatus(AttendanceStatusEnum.LATE.getValue());
                attendance.setLateMinutes((int) ChronoUnit.MINUTES.between(workStart, now));
            } else {
                attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());
            }

            attendance.setRemark(dto.getRemark());
            attendanceMapper.insert(attendance);
        } else {
            attendance.setCheckInTime(now);
            attendance.setCheckInLatitude(dto.getLatitude());
            attendance.setCheckInLongitude(dto.getLongitude());
            attendance.setCheckInAddress(dto.getAddress());
            
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

        log.info("用户上班打卡成功，用户ID：{}，状态：{}", userId, attendance.getStatus());
        return convertToVO(attendance, user.getRealName());
    }

    @Override
    @Transactional
    public AttendanceVO checkOut(CheckOutDTO dto) {
        Long userId = getCurrentUserId();
        log.info("用户下班打卡，用户ID：{}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "用户不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        UserAttendance attendance = attendanceMapper.selectByUserIdAndDate(userId, today);

        if (attendance == null) {
            attendance = new UserAttendance();
            attendance.setUserId(userId);
            attendance.setAttendanceDate(today);
            attendance.setCheckOutTime(now);
            attendance.setCheckOutLatitude(dto.getLatitude());
            attendance.setCheckOutLongitude(dto.getLongitude());
            attendance.setCheckOutAddress(dto.getAddress());
            attendance.setStatus(AttendanceStatusEnum.ABSENT.getValue());
            attendance.setRemark("未打上班卡，直接打下班卡");
            attendance.setCreateTime(now);
            attendanceMapper.insert(attendance);
        } else {
            attendance.setCheckOutTime(now);
            attendance.setCheckOutLatitude(dto.getLatitude());
            attendance.setCheckOutLongitude(dto.getLongitude());
            attendance.setCheckOutAddress(dto.getAddress());

            LocalDateTime workEnd = LocalDateTime.of(today, attendanceProperties.getWorkEndTime());
            if (now.isBefore(workEnd.minusMinutes(attendanceProperties.getEarlyLeaveThreshold()))) {
                attendance.setEarlyLeaveMinutes((int) ChronoUnit.MINUTES.between(now, workEnd));
                if (attendance.getStatus() == AttendanceStatusEnum.NORMAL.getValue()) {
                    attendance.setStatus(AttendanceStatusEnum.EARLY_LEAVE.getValue());
                } else if (attendance.getStatus() == AttendanceStatusEnum.LATE.getValue()) {
                    attendance.setStatus(AttendanceStatusEnum.LATE_AND_EARLY_LEAVE.getValue());
                }
            }

            if (dto.getRemark() != null) {
                attendance.setRemark(dto.getRemark());
            }
            attendanceMapper.updateById(attendance);
        }

        log.info("用户下班打卡成功，用户ID：{}，状态：{}", userId, attendance.getStatus());
        return convertToVO(attendance, user.getRealName());
    }

    @Override
    public AttendanceVO getAttendanceByDate(Long userId, LocalDate date) {
        UserAttendance attendance = attendanceMapper.selectByUserIdAndDate(userId, date);
        if (attendance == null) {
            return null;
        }

        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getRealName() : null;

        return convertToVO(attendance, userName);
    }

    @Override
    public List<AttendanceVO> getAttendanceListByMonth(Long userId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<UserAttendance> list = attendanceMapper.selectByUserIdAndMonth(userId, startDate, endDate);

        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getRealName() : null;

        return list.stream()
                .map(attendance -> convertToVO(attendance, userName))
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceStatisticsVO getAttendanceStatistics(Long userId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        int shouldAttendanceDays = calculateWorkDays(startDate, endDate);

        Integer normalDays = attendanceMapper.countNormalDays(userId, startDate, endDate);
        if (normalDays == null) {
            normalDays = 0;
        }

        Integer lateTimes = attendanceMapper.countLateTimes(userId, startDate, endDate);
        if (lateTimes == null) {
            lateTimes = 0;
        }

        Integer earlyLeaveTimes = attendanceMapper.countEarlyLeaveTimes(userId, startDate, endDate);
        if (earlyLeaveTimes == null) {
            earlyLeaveTimes = 0;
        }

        Integer lateAndEarlyLeaveTimes = attendanceMapper.countLateAndEarlyLeaveDays(userId, startDate, endDate);
        if (lateAndEarlyLeaveTimes == null) {
            lateAndEarlyLeaveTimes = 0;
        }

        Integer leaveDays = attendanceMapper.countLeaveDays(userId, startDate, endDate);
        if (leaveDays == null) {
            leaveDays = 0;
        }

        int actualAttendanceDays = normalDays + lateTimes + earlyLeaveTimes + lateAndEarlyLeaveTimes;
        int absentDays = shouldAttendanceDays - actualAttendanceDays - leaveDays;
        if (absentDays < 0) {
            absentDays = 0;
        }

        double attendanceRate = shouldAttendanceDays > 0 
                ? (double) actualAttendanceDays / shouldAttendanceDays * 100 
                : 0;

        User user = userMapper.selectById(userId);

        AttendanceStatisticsVO statistics = new AttendanceStatisticsVO();
        statistics.setUserId(userId != null ? userId.toString() : null);
        statistics.setUserName(user != null ? user.getRealName() : null);
        statistics.setMonth(yearMonth);
        statistics.setShouldAttendanceDays(shouldAttendanceDays);
        statistics.setActualAttendanceDays(actualAttendanceDays);
        statistics.setNormalDays(normalDays);
        statistics.setLateTimes(lateTimes + lateAndEarlyLeaveTimes);
        statistics.setEarlyLeaveTimes(earlyLeaveTimes + lateAndEarlyLeaveTimes);
        statistics.setAbsentDays(absentDays);
        statistics.setLeaveDays(leaveDays);
        statistics.setAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);

        return statistics;
    }

    @Override
    @Transactional
    public AttendanceVO supplementAttendance(Long attendanceId, LocalDateTime checkInTime, LocalDateTime checkOutTime, Integer status, String remark) {
        UserAttendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "出勤记录不存在");
        }

        if (checkInTime != null) {
            attendance.setCheckInTime(checkInTime);
        }
        if (checkOutTime != null) {
            attendance.setCheckOutTime(checkOutTime);
        }
        if (status != null) {
            attendance.setStatus(status);
        }
        if (remark != null) {
            attendance.setRemark(remark);
        }

        attendanceMapper.updateById(attendance);

        User user = userMapper.selectById(attendance.getUserId());
        String userName = user != null ? user.getRealName() : null;

        log.info("补卡成功，出勤记录ID：{}", attendanceId);
        return convertToVO(attendance, userName);
    }

    @Override
    @Transactional
    public AttendanceVO applyLeave(Long userId, Integer leaveType, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        log.info("用户请假申请：用户ID={}, 请假类型={}, 开始时间={}, 结束时间={}", userId, leaveType, startTime, endTime);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            UserAttendance existingAttendance = attendanceMapper.selectByUserIdAndDate(userId, currentDate);
            
            if (existingAttendance != null && existingAttendance.getStatus() != AttendanceStatusEnum.ABSENT.getValue()) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            UserAttendance leaveAttendance = existingAttendance != null ? existingAttendance : new UserAttendance();
            leaveAttendance.setUserId(userId);
            leaveAttendance.setAttendanceDate(currentDate);
            leaveAttendance.setStatus(AttendanceStatusEnum.LEAVE.getValue());
            leaveAttendance.setLeaveType(leaveType);
            leaveAttendance.setLeaveStartTime(currentDate.equals(startDate) ? startTime : null);
            leaveAttendance.setLeaveEndTime(currentDate.equals(endDate) ? endTime : null);
            leaveAttendance.setRemark(reason);
            leaveAttendance.setUpdateTime(LocalDateTime.now());
            
            if (existingAttendance == null) {
                leaveAttendance.setCreateTime(LocalDateTime.now());
                attendanceMapper.insert(leaveAttendance);
            } else {
                attendanceMapper.updateById(leaveAttendance);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        UserAttendance firstLeaveRecord = attendanceMapper.selectByUserIdAndDate(userId, startDate);
        log.info("请假申请成功：用户ID={}, 请假天数={}", userId, ChronoUnit.DAYS.between(startDate, endDate) + 1);
        
        reassignDriverTasks(userId);
        
        return convertToVO(firstLeaveRecord, user.getRealName());
    }
    
    private void reassignDriverTasks(Long userId) {
        try {
            String roleCode = userService.getRoleCodeByUserId(userId);
            if (roleCode == null) {
                log.info("用户无角色，无需重新分配任务：userId={}", userId);
                return;
            }
            
            log.info("用户请假，尝试重新分配任务：userId={}, roleCode={}", userId, roleCode);
            
            dispatchClient.reassignTasksByUserLeave(userId, roleCode);
            log.info("用户请假任务重新分配完成：userId={}, roleCode={}", userId, roleCode);
        } catch (Exception e) {
            log.error("重新分配用户任务失败：userId={}, 错误={}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean cancelLeave(Long attendanceId) {
        UserAttendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND, "出勤记录不存在");
        }
        
        if (attendance.getStatus() != AttendanceStatusEnum.LEAVE.getValue()) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "该记录不是请假记录");
        }
        
        attendance.setStatus(AttendanceStatusEnum.ABSENT.getValue());
        attendance.setLeaveType(null);
        attendance.setLeaveStartTime(null);
        attendance.setLeaveEndTime(null);
        attendance.setRemark("已取消请假");
        attendance.setUpdateTime(LocalDateTime.now());
        
        attendanceMapper.updateById(attendance);
        log.info("取消请假成功：出勤记录ID={}", attendanceId);
        
        return true;
    }

    @Override
    public List<AttendanceVO> getLeaveList(Long userId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        
        List<UserAttendance> list = attendanceMapper.selectByUserIdAndMonth(userId, startDate, endDate);
        
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getRealName() : null;
        
        return list.stream()
                .filter(a -> a.getStatus() == AttendanceStatusEnum.LEAVE.getValue())
                .map(attendance -> convertToVO(attendance, userName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getAvailableDriverIds() {
        LocalDate today = LocalDate.now();
        
        List<UserAttendance> leaveRecords = attendanceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAttendance>()
                .eq(UserAttendance::getAttendanceDate, today)
                .eq(UserAttendance::getStatus, AttendanceStatusEnum.LEAVE.getValue())
        );
        
        List<Long> leaveUserIds = leaveRecords.stream()
                .map(UserAttendance::getUserId)
                .collect(Collectors.toList());
        
        List<User> allDrivers = userMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .isNotNull(User::getRoleId)
                .eq(User::getStatus, 1)
        );
        
        return allDrivers.stream()
                .map(User::getId)
                .filter(id -> !leaveUserIds.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return com.klzw.common.auth.context.UserContext.getUserId();
    }

    /**
     * 转换为 VO
     */
    private AttendanceVO convertToVO(UserAttendance attendance, String userName) {
        AttendanceVO vo = new AttendanceVO();
        vo.setId(attendance.getId() != null ? attendance.getId().toString() : null);
        vo.setUserId(attendance.getUserId() != null ? attendance.getUserId().toString() : null);
        vo.setUserName(userName);
        vo.setAttendanceDate(attendance.getAttendanceDate());
        vo.setCheckInTime(attendance.getCheckInTime());
        vo.setCheckOutTime(attendance.getCheckOutTime());
        vo.setCheckInLatitude(attendance.getCheckInLatitude());
        vo.setCheckInLongitude(attendance.getCheckInLongitude());
        vo.setCheckInAddress(attendance.getCheckInAddress());
        vo.setCheckOutLatitude(attendance.getCheckOutLatitude());
        vo.setCheckOutLongitude(attendance.getCheckOutLongitude());
        vo.setCheckOutAddress(attendance.getCheckOutAddress());
        vo.setStatus(attendance.getStatus());
        vo.setLateMinutes(attendance.getLateMinutes());
        vo.setEarlyLeaveMinutes(attendance.getEarlyLeaveMinutes());
        vo.setLeaveType(attendance.getLeaveType());
        vo.setLeaveStartTime(attendance.getLeaveStartTime());
        vo.setLeaveEndTime(attendance.getLeaveEndTime());
        vo.setRemark(attendance.getRemark());

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
            if (date.getDayOfWeek().getValue() <= 5) {
                workDays++;
            }
            date = date.plusDays(1);
        }
        return workDays;
    }
}
