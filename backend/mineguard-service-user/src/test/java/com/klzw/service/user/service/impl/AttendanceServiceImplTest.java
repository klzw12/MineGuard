package com.klzw.service.user.service.impl;

import com.klzw.service.user.config.AttendanceProperties;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserAttendance;
import com.klzw.service.user.enums.AttendanceStatusEnum;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceServiceImplTest {

    @Mock
    private UserAttendanceMapper attendanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AttendanceProperties attendanceProperties;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(attendanceProperties.getWorkStartTime()).thenReturn(LocalTime.of(9, 0));
        when(attendanceProperties.getWorkEndTime()).thenReturn(LocalTime.of(18, 0));
        when(attendanceProperties.getLateThreshold()).thenReturn(15);
        when(attendanceProperties.getEarlyLeaveThreshold()).thenReturn(15);
    }

    @Test
    void testGetAttendanceByDate_NotFound() {
        when(attendanceMapper.selectByUserIdAndDate(123L, LocalDate.now())).thenReturn(null);

        AttendanceVO result = attendanceService.getAttendanceByDate(123L, LocalDate.now());

        assertNull(result);
    }

    @Test
    void testGetAttendanceByDate_Found() {
        UserAttendance attendance = new UserAttendance();
        attendance.setId(1L);
        attendance.setUserId(123L);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());

        User user = new User();
        user.setId(123L);
        user.setRealName("张三");

        when(attendanceMapper.selectByUserIdAndDate(123L, LocalDate.now())).thenReturn(attendance);
        when(userMapper.selectById(123L)).thenReturn(user);

        AttendanceVO result = attendanceService.getAttendanceByDate(123L, LocalDate.now());

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("123", result.getUserId());
        assertEquals("张三", result.getUserName());
    }

    @Test
    void testGetAttendanceListByMonth() {
        String yearMonth = "2026-03";
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        UserAttendance attendance = new UserAttendance();
        attendance.setId(1L);
        attendance.setUserId(123L);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());

        User user = new User();
        user.setId(123L);
        user.setRealName("张三");

        when(attendanceMapper.selectByUserIdAndMonth(123L, startDate, endDate))
                .thenReturn(Collections.singletonList(attendance));
        when(userMapper.selectById(123L)).thenReturn(user);

        List<AttendanceVO> result = attendanceService.getAttendanceListByMonth(123L, yearMonth);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("123", result.get(0).getUserId());
        assertEquals("张三", result.get(0).getUserName());
    }

    @Test
    void testGetAttendanceStatistics() {
        String yearMonth = "2026-03";
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        User user = new User();
        user.setId(123L);
        user.setRealName("张三");

        when(attendanceMapper.countAttendanceDays(123L, startDate, endDate)).thenReturn(20);
        when(attendanceMapper.countLateTimes(123L, startDate, endDate)).thenReturn(2);
        when(attendanceMapper.countEarlyLeaveTimes(123L, startDate, endDate)).thenReturn(1);
        when(userMapper.selectById(123L)).thenReturn(user);

        AttendanceStatisticsVO result = attendanceService.getAttendanceStatistics(123L, yearMonth);

        assertNotNull(result);
        assertEquals("123", result.getUserId());
        assertEquals("张三", result.getUserName());
        assertEquals(yearMonth, result.getMonth());
        assertEquals(20, result.getActualAttendanceDays());
        assertEquals(2, result.getLateTimes());
        assertEquals(1, result.getEarlyLeaveTimes());
    }

    @Test
    void testSupplementAttendance_NotFound() {
        when(attendanceMapper.selectById(1L)).thenReturn(null);

        UserException exception = assertThrows(UserException.class, () -> {
            attendanceService.supplementAttendance(1L, LocalDateTime.now(), LocalDateTime.now(), 1, "补卡");
        });
        assertNotNull(exception);
    }

    @Test
    void testSupplementAttendance_Success() {
        UserAttendance attendance = new UserAttendance();
        attendance.setId(1L);
        attendance.setUserId(123L);
        attendance.setAttendanceDate(LocalDate.now());

        User user = new User();
        user.setId(123L);
        user.setRealName("张三");

        when(attendanceMapper.selectById(1L)).thenReturn(attendance);
        when(userMapper.selectById(123L)).thenReturn(user);

        AttendanceVO result = attendanceService.supplementAttendance(1L, LocalDateTime.now(), LocalDateTime.now(), 1, "补卡");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("123", result.getUserId());
        assertEquals("张三", result.getUserName());

        verify(attendanceMapper, times(1)).updateById(any(UserAttendance.class));
    }
}
