package com.klzw.service.user.service.impl;

import com.klzw.service.user.config.AttendanceProperties;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.DriverAttendance;
import com.klzw.service.user.enums.AttendanceStatusEnum;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.mapper.DriverAttendanceMapper;
import com.klzw.service.user.mapper.DriverMapper;
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
    private DriverAttendanceMapper attendanceMapper;

    @Mock
    private DriverMapper driverMapper;

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
    void testCheckIn_DriverNotFound() {
        CheckInDTO dto = new CheckInDTO();
        dto.setDriverId(123L);

        when(driverMapper.selectById(123L)).thenReturn(null);

        UserException exception = assertThrows(UserException.class, () -> {
            attendanceService.checkIn(dto);
        });
        assertNotNull(exception);
    }

    @Test
    void testCheckIn_NewAttendance() {
        CheckInDTO dto = new CheckInDTO();
        dto.setDriverId(123L);

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(driverMapper.selectById(123L)).thenReturn(driver);
        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(null);

        AttendanceVO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        assertEquals("123", result.getDriverId());
        assertEquals("张三", result.getDriverName());
        assertNotNull(result.getCheckInTime());

        verify(attendanceMapper, times(1)).insert(any(DriverAttendance.class));
    }

    @Test
    void testCheckIn_RepeatAttendance() {
        CheckInDTO dto = new CheckInDTO();
        dto.setDriverId(123L);

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        DriverAttendance existingAttendance = new DriverAttendance();
        existingAttendance.setId(1L);
        existingAttendance.setDriverId(123L);
        existingAttendance.setAttendanceDate(LocalDate.now());
        existingAttendance.setCheckInTime(LocalDateTime.now().minusHours(1));

        when(driverMapper.selectById(123L)).thenReturn(driver);
        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(existingAttendance);

        AttendanceVO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        assertEquals("123", result.getDriverId());

        verify(attendanceMapper, times(1)).updateById(any(DriverAttendance.class));
    }

    @Test
    void testCheckOut_DriverNotFound() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setDriverId(123L);

        when(driverMapper.selectById(123L)).thenReturn(null);

        UserException exception = assertThrows(UserException.class, () -> {
            attendanceService.checkOut(dto);
        });
        assertNotNull(exception);
    }

    @Test
    void testCheckOut_NoCheckIn() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setDriverId(123L);

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(driverMapper.selectById(123L)).thenReturn(driver);
        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(null);

        AttendanceVO result = attendanceService.checkOut(dto);

        assertNotNull(result);
        assertEquals("123", result.getDriverId());
        assertEquals("张三", result.getDriverName());
        assertNotNull(result.getCheckOutTime());

        verify(attendanceMapper, times(1)).insert(any(DriverAttendance.class));
    }

    @Test
    void testCheckOut_WithCheckIn() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setDriverId(123L);

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        DriverAttendance existingAttendance = new DriverAttendance();
        existingAttendance.setId(1L);
        existingAttendance.setDriverId(123L);
        existingAttendance.setAttendanceDate(LocalDate.now());
        existingAttendance.setCheckInTime(LocalDateTime.now().minusHours(8));
        existingAttendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());

        when(driverMapper.selectById(123L)).thenReturn(driver);
        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(existingAttendance);

        AttendanceVO result = attendanceService.checkOut(dto);

        assertNotNull(result);
        assertEquals("123", result.getDriverId());
        assertNotNull(result.getCheckOutTime());

        verify(attendanceMapper, times(1)).updateById(any(DriverAttendance.class));
    }

    @Test
    void testGetAttendanceByDate_NotFound() {
        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(null);

        AttendanceVO result = attendanceService.getAttendanceByDate(123L, LocalDate.now());

        assertNull(result);
    }

    @Test
    void testGetAttendanceByDate_Found() {
        DriverAttendance attendance = new DriverAttendance();
        attendance.setId(1L);
        attendance.setDriverId(123L);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(attendanceMapper.selectByDriverIdAndDate(123L, LocalDate.now())).thenReturn(attendance);
        when(driverMapper.selectById(123L)).thenReturn(driver);

        AttendanceVO result = attendanceService.getAttendanceByDate(123L, LocalDate.now());

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("123", result.getDriverId());
        assertEquals("张三", result.getDriverName());
    }

    @Test
    void testGetAttendanceListByMonth() {
        String yearMonth = "2026-03";
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        DriverAttendance attendance = new DriverAttendance();
        attendance.setId(1L);
        attendance.setDriverId(123L);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(attendanceMapper.selectByDriverIdAndMonth(123L, startDate, endDate))
                .thenReturn(Collections.singletonList(attendance));
        when(driverMapper.selectById(123L)).thenReturn(driver);

        List<AttendanceVO> result = attendanceService.getAttendanceListByMonth(123L, yearMonth);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("123", result.get(0).getDriverId());
        assertEquals("张三", result.get(0).getDriverName());
    }

    @Test
    void testGetAttendanceStatistics() {
        String yearMonth = "2026-03";
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(attendanceMapper.countAttendanceDays(123L, startDate, endDate)).thenReturn(20);
        when(attendanceMapper.countLateTimes(123L, startDate, endDate)).thenReturn(2);
        when(attendanceMapper.countEarlyLeaveTimes(123L, startDate, endDate)).thenReturn(1);
        when(driverMapper.selectById(123L)).thenReturn(driver);

        AttendanceStatisticsVO result = attendanceService.getAttendanceStatistics(123L, yearMonth);

        assertNotNull(result);
        assertEquals("123", result.getDriverId());
        assertEquals("张三", result.getDriverName());
        assertEquals(yearMonth, result.getMonth());
        assertEquals(20, result.getActualAttendanceDays());
        assertEquals(2, result.getLateTimes());
        assertEquals(1, result.getEarlyLeaveTimes());
    }

    @Test
    void testSupplementAttendance_NotFound() {
        when(attendanceMapper.selectById(1L)).thenReturn(null);

        UserException exception = assertThrows(UserException.class, () -> {
            attendanceService.supplementAttendance(1L, LocalDate.now(), LocalDate.now(), 1, "补卡");
        });
        assertNotNull(exception);
    }

    @Test
    void testSupplementAttendance_Success() {
        DriverAttendance attendance = new DriverAttendance();
        attendance.setId(1L);
        attendance.setDriverId(123L);
        attendance.setAttendanceDate(LocalDate.now());

        Driver driver = new Driver();
        driver.setId(123L);
        driver.setDriverName("张三");

        when(attendanceMapper.selectById(1L)).thenReturn(attendance);
        when(driverMapper.selectById(123L)).thenReturn(driver);

        AttendanceVO result = attendanceService.supplementAttendance(1L, LocalDate.now(), LocalDate.now(), 1, "补卡");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("123", result.getDriverId());
        assertEquals("张三", result.getDriverName());

        verify(attendanceMapper, times(1)).updateById(any(DriverAttendance.class));
    }
}
