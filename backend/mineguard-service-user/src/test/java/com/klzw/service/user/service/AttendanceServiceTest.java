package com.klzw.service.user.service;

import com.klzw.common.auth.context.UserContext;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.service.user.dto.CheckInDTO;
import com.klzw.service.user.dto.CheckOutDTO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.UserAttendance;
import com.klzw.service.user.enums.AttendanceStatusEnum;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.properties.AttendanceProperties;
import com.klzw.service.user.service.impl.AttendanceServiceImpl;
import com.klzw.service.user.vo.AttendanceStatisticsVO;
import com.klzw.service.user.vo.AttendanceVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AttendanceService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Mock
    private UserAttendanceMapper attendanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AttendanceProperties attendanceProperties;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private DispatchClient dispatchClient;

    @Mock
    private UserService userService;

    private User testUser;
    private UserAttendance testAttendance;
    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setDeleted(0);

        testAttendance = new UserAttendance();
        testAttendance.setId(1L);
        testAttendance.setUserId(1L);
        testAttendance.setAttendanceDate(LocalDate.now());
        testAttendance.setCheckInTime(LocalDateTime.now());
        testAttendance.setStatus(AttendanceStatusEnum.NORMAL.getValue());
        testAttendance.setCreateTime(LocalDateTime.now());
        testAttendance.setDeleted(0);

        userContextMockedStatic = mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getUserId).thenReturn(1L);

        lenient().when(attendanceProperties.getWorkStartTime()).thenReturn(LocalTime.of(9, 0));
        lenient().when(attendanceProperties.getWorkEndTime()).thenReturn(LocalTime.of(18, 0));
        lenient().when(attendanceProperties.getLateThreshold()).thenReturn(15);
        lenient().when(attendanceProperties.getEarlyLeaveThreshold()).thenReturn(15);
    }

    @AfterEach
    void tearDown() {
        reset(attendanceMapper, userMapper, driverMapper, dispatchClient, userService);
        userContextMockedStatic.close();
    }

    /**
     * 测试签到
     */
    @Test
    void testCheckIn() {
        CheckInDTO dto = new CheckInDTO();
        dto.setLatitude(39.9042);
        dto.setLongitude(116.4074);
        dto.setAddress("北京市东城区");
        dto.setRemark("正常签到");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(attendanceMapper.selectByUserIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);
        when(attendanceMapper.insert(any(UserAttendance.class))).thenAnswer(invocation -> {
            UserAttendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return 1;
        });

        AttendanceVO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        assertEquals("1", result.getUserId());
        assertEquals("测试用户", result.getUserName());
        verify(attendanceMapper, times(1)).insert(any(UserAttendance.class));
    }

    /**
     * 测试签到（用户不存在）
     */
    @Test
    void testCheckInUserNotFound() {
        CheckInDTO dto = new CheckInDTO();
        dto.setLatitude(39.9042);
        dto.setLongitude(116.4074);

        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(UserException.class, () -> {
            attendanceService.checkIn(dto);
        });

        verify(attendanceMapper, never()).insert(any(UserAttendance.class));
    }

    /**
     * 测试签退
     */
    @Test
    void testCheckOut() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setLatitude(39.9042);
        dto.setLongitude(116.4074);
        dto.setAddress("北京市东城区");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(attendanceMapper.selectByUserIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(testAttendance);
        when(attendanceMapper.updateById(any(UserAttendance.class))).thenReturn(1);

        AttendanceVO result = attendanceService.checkOut(dto);

        assertNotNull(result);
        verify(attendanceMapper, times(1)).updateById(any(UserAttendance.class));
    }

    /**
     * 测试签退（用户不存在）
     */
    @Test
    void testCheckOutUserNotFound() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setLatitude(39.9042);
        dto.setLongitude(116.4074);

        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(UserException.class, () -> {
            attendanceService.checkOut(dto);
        });

        verify(attendanceMapper, never()).updateById(any(UserAttendance.class));
    }

    /**
     * 测试签退（无签到记录）
     */
    @Test
    void testCheckOutWithoutCheckIn() {
        CheckOutDTO dto = new CheckOutDTO();
        dto.setLatitude(39.9042);
        dto.setLongitude(116.4074);
        dto.setAddress("北京市东城区");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(attendanceMapper.selectByUserIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);
        when(attendanceMapper.insert(any(UserAttendance.class))).thenAnswer(invocation -> {
            UserAttendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return 1;
        });

        AttendanceVO result = attendanceService.checkOut(dto);

        assertNotNull(result);
        verify(attendanceMapper, times(1)).insert(any(UserAttendance.class));
    }

    /**
     * 测试获取出勤统计
     */
    @Test
    void testGetAttendanceStatistics() {
        String yearMonth = "2024-01";

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(attendanceMapper.countNormalDays(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(20);
        when(attendanceMapper.countLateTimes(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(2);
        when(attendanceMapper.countEarlyLeaveTimes(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(1);
        when(attendanceMapper.countLateAndEarlyLeaveDays(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(0);
        when(attendanceMapper.countLeaveDays(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(1);

        AttendanceStatisticsVO result = attendanceService.getAttendanceStatistics(1L, yearMonth);

        assertNotNull(result);
        assertEquals(20, result.getNormalDays());
        assertEquals(2, result.getLateTimes());
        assertEquals(1, result.getEarlyLeaveTimes());
    }

    /**
     * 测试获取可用司机ID列表
     */
    @Test
    void testGetAvailableDriverIds() {
        User driver1 = new User();
        driver1.setId(1L);
        driver1.setRoleId(1L);
        driver1.setStatus(1);

        User driver2 = new User();
        driver2.setId(2L);
        driver2.setRoleId(1L);
        driver2.setStatus(1);

        UserAttendance leaveRecord = new UserAttendance();
        leaveRecord.setUserId(1L);
        leaveRecord.setStatus(AttendanceStatusEnum.LEAVE.getValue());

        when(attendanceMapper.selectList(any())).thenReturn(Collections.singletonList(leaveRecord));
        when(userMapper.selectList(any())).thenReturn(List.of(driver1, driver2));

        List<Long> result = attendanceService.getAvailableDriverIds();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0));
    }
}
