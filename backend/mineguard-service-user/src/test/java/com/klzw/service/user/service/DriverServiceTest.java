package com.klzw.service.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.auth.enums.RoleEnum;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.DriverVehicle;
import com.klzw.service.user.entity.Repairman;
import com.klzw.service.user.entity.SafetyOfficer;
import com.klzw.service.user.enums.DriverStatusEnum;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.DriverVehicleMapper;
import com.klzw.service.user.mapper.RepairmanMapper;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.SafetyOfficerMapper;
import com.klzw.service.user.mapper.UserAttendanceMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.impl.DriverServiceImpl;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DriverService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @InjectMocks
    private DriverServiceImpl driverService;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private DriverVehicleMapper driverVehicleMapper;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private RepairmanMapper repairmanMapper;

    @Mock
    private SafetyOfficerMapper safetyOfficerMapper;

    @Mock
    private UserAttendanceMapper userAttendanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    private Driver testDriver;
    private DriverVehicle testDriverVehicle;

    @BeforeEach
    void setUp() {
        // 初始化测试司机数据
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setUserId(100L);
        testDriver.setDriverName("测试司机");
        testDriver.setGender(1);
        testDriver.setIdCard("110101199001011234");
        testDriver.setLicenseType("A1");
        testDriver.setDrivingYears(10);
        testDriver.setStatus(DriverStatusEnum.EMPLOYED.getValue());
        testDriver.setScore(85);
        testDriver.setCreateTime(LocalDateTime.now());
        testDriver.setUpdateTime(LocalDateTime.now());
        testDriver.setDeleted(0);

        // 初始化测试司机车辆关联数据
        testDriverVehicle = new DriverVehicle();
        testDriverVehicle.setId(1L);
        testDriverVehicle.setDriverId(1L);
        testDriverVehicle.setVehicleId(100L);
        testDriverVehicle.setUseCount(5);
        testDriverVehicle.setIsDefault(1);
        testDriverVehicle.setCreateTime(LocalDateTime.now());
        testDriverVehicle.setUpdateTime(LocalDateTime.now());
        testDriverVehicle.setDeleted(0);
    }

    @AfterEach
    void tearDown() {
        reset(driverMapper, driverVehicleMapper, vehicleClient, repairmanMapper, safetyOfficerMapper, userAttendanceMapper, userMapper, roleMapper);
    }

    /**
     * 测试根据ID获取司机
     */
    @Test
    void testGetById() {
        when(driverMapper.selectById(1L)).thenReturn(testDriver);
        when(driverVehicleMapper.selectByDriverId(100L)).thenReturn(Collections.singletonList(testDriverVehicle));

        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleNo", "京A12345");
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);

        DriverVO result = driverService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试司机", result.getDriverName());
        assertEquals(DriverStatusEnum.EMPLOYED.getValue(), result.getStatus());
        verify(driverMapper, times(1)).selectById(1L);
    }

    /**
     * 测试根据ID获取不存在的司机
     */
    @Test
    void testGetByIdNotFound() {
        when(driverMapper.selectById(999L)).thenReturn(null);

        DriverVO result = driverService.getById(999L);

        assertNull(result);
        verify(driverMapper, times(1)).selectById(999L);
    }

    /**
     * 测试根据用户ID获取司机
     */
    @Test
    void testGetByUserId() {
        User testUser = new User();
        testUser.setId(100L);
        testUser.setRealName("测试司机");
        testUser.setGender(1);
        testUser.setIdCard("110101199001011234");
        testUser.setIdCardFrontUrl("front.jpg");
        testUser.setIdCardBackUrl("back.jpg");
        when(userMapper.selectById(100L)).thenReturn(testUser);
        
        when(driverMapper.selectByUserId(anyString())).thenReturn(testDriver);
        when(driverVehicleMapper.selectByDriverId(100L)).thenReturn(Collections.singletonList(testDriverVehicle));

        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleNo", "京A12345");
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);

        DriverVO result = driverService.getByUserId(100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getUserId());
        verify(driverMapper, times(1)).selectByUserId(anyString());
    }

    /**
     * 测试根据用户ID获取不存在的司机
     */
    @Test
    void testGetByUserIdNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        DriverVO result = driverService.getByUserId(999L);

        assertNull(result);
        verify(userMapper, times(1)).selectById(999L);
    }

    /**
     * 测试获取司机列表
     */
    @Test
    void testGetList() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);

        List<DriverVO> result = driverService.getList("测试", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试司机", result.get(0).getDriverName());
        verify(driverMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取司机列表（无筛选条件）
     */
    @Test
    void testGetListWithoutFilter() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);

        List<DriverVO> result = driverService.getList(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(driverMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试更新司机状态
     */
    @Test
    void testUpdateStatus() {
        when(driverMapper.selectById(1L)).thenReturn(testDriver);
        when(driverMapper.updateById(any(Driver.class))).thenReturn(1);

        driverService.updateStatus(1L, DriverStatusEnum.ON_LEAVE.getValue());

        verify(driverMapper, times(1)).selectById(1L);
        verify(driverMapper, times(1)).updateById(any(Driver.class));
    }

    /**
     * 测试更新不存在司机的状态
     */
    @Test
    void testUpdateStatusNotFound() {
        when(driverMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            driverService.updateStatus(999L, DriverStatusEnum.ON_LEAVE.getValue());
        });

        verify(driverMapper, times(1)).selectById(999L);
        verify(driverMapper, never()).updateById(any(Driver.class));
    }

    /**
     * 测试获取可用司机
     */
    @Test
    void testGetAvailableDrivers() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);
        when(driverMapper.hasRole(100L, RoleEnum.DRIVER.getValue())).thenReturn(true);

        List<DriverVO> result = driverService.getAvailableDrivers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(driverMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取可用司机（无司机）
     */
    @Test
    void testGetAvailableDriversEmpty() {
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<DriverVO> result = driverService.getAvailableDrivers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试选择最佳司机
     */
    @Test
    void testSelectBestDriver() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);
        when(driverMapper.hasRole(100L, RoleEnum.DRIVER.getValue())).thenReturn(true);
        when(driverVehicleMapper.selectByDriverId(1L)).thenReturn(Collections.singletonList(testDriverVehicle));
        when(driverMapper.findBusyDriverIds(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(userAttendanceMapper.countNormalDays(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(20);
        when(userAttendanceMapper.countLateAndEarlyLeaveDays(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(2);
        when(driverMapper.countCompletedTrips(100L)).thenReturn(50);
        when(driverMapper.countTotalTrips(100L)).thenReturn(55);

        DriverVO result = driverService.selectBestDriver(null, null);

        assertNotNull(result);
        assertEquals("测试司机", result.getDriverName());
    }

    /**
     * 测试选择最佳司机（无可用司机）
     */
    @Test
    void testSelectBestDriverNoAvailable() {
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        DriverVO result = driverService.selectBestDriver(null, null);

        assertNull(result);
    }

    /**
     * 测试选择最佳司机（指定车辆）
     */
    @Test
    void testSelectBestDriverWithVehicle() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);
        when(driverMapper.hasRole(100L, RoleEnum.DRIVER.getValue())).thenReturn(true);
        when(driverVehicleMapper.selectByDriverId(100L)).thenReturn(Collections.singletonList(testDriverVehicle));
        when(driverMapper.findBusyDriverIds(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(userAttendanceMapper.countNormalDays(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(20);
        when(userAttendanceMapper.countLateAndEarlyLeaveDays(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(2);
        when(driverMapper.countCompletedTrips(100L)).thenReturn(50);
        when(driverMapper.countTotalTrips(100L)).thenReturn(55);

        DriverVO result = driverService.selectBestDriver(100L, null);

        assertNotNull(result);
        assertEquals("测试司机", result.getDriverName());
    }

    /**
     * 测试添加常用车辆
     */
    @Test
    void testAddCommonVehicle() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setRoleId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        
        Role driverRole = new Role();
        driverRole.setId(1L);
        driverRole.setRoleCode("DRIVER");
        when(roleMapper.selectById(1L)).thenReturn(driverRole);
        
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleType", 1);
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);
        
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 100L)).thenReturn(null);
        when(driverVehicleMapper.selectByDriverId(1L)).thenReturn(Collections.emptyList());
        when(driverVehicleMapper.insert(any(DriverVehicle.class))).thenReturn(1);

        driverService.addCommonVehicle(1L, 100L);

        verify(userMapper, times(1)).selectById(1L);
        verify(driverVehicleMapper, times(1)).insert(any(DriverVehicle.class));
    }

    /**
     * 测试添加常用车辆（司机不存在）
     */
    @Test
    void testAddCommonVehicleDriverNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            driverService.addCommonVehicle(999L, 100L);
        });

        verify(userMapper, times(1)).selectById(999L);
        verify(driverVehicleMapper, never()).insert(any(DriverVehicle.class));
    }

    /**
     * 测试添加常用车辆（已存在）
     */
    @Test
    void testAddCommonVehicleAlreadyExists() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setRoleId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        
        Role driverRole = new Role();
        driverRole.setId(1L);
        driverRole.setRoleCode("DRIVER");
        when(roleMapper.selectById(1L)).thenReturn(driverRole);
        
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleType", 1);
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);
        
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 100L)).thenReturn(testDriverVehicle);

        driverService.addCommonVehicle(1L, 100L);

        verify(driverVehicleMapper, never()).insert(any(DriverVehicle.class));
    }

    /**
     * 测试设置默认车辆
     */
    @Test
    void testSetDefaultVehicle() {
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 100L)).thenReturn(testDriverVehicle);
        when(driverVehicleMapper.clearDefaultByDriverId(1L)).thenReturn(1);
        when(driverVehicleMapper.updateById(any(DriverVehicle.class))).thenReturn(1);

        driverService.setDefaultVehicle(1L, 100L);

        verify(driverVehicleMapper, times(1)).selectByDriverAndVehicle(1L, 100L);
        verify(driverVehicleMapper, times(1)).clearDefaultByDriverId(1L);
        verify(driverVehicleMapper, times(1)).updateById(any(DriverVehicle.class));
    }

    /**
     * 测试设置默认车辆（关联不存在）
     */
    @Test
    void testSetDefaultVehicleNotFound() {
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            driverService.setDefaultVehicle(1L, 999L);
        });

        verify(driverVehicleMapper, times(1)).selectByDriverAndVehicle(1L, 999L);
        verify(driverVehicleMapper, never()).clearDefaultByDriverId(anyLong());
    }

    /**
     * 测试移除常用车辆
     */
    @Test
    void testRemoveCommonVehicle() {
        testDriverVehicle.setIsDefault(0);
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 100L)).thenReturn(testDriverVehicle);
        when(driverVehicleMapper.updateById(any(DriverVehicle.class))).thenReturn(1);

        driverService.removeCommonVehicle(1L, 100L);

        verify(driverVehicleMapper, times(1)).selectByDriverAndVehicle(1L, 100L);
        verify(driverVehicleMapper, times(1)).updateById(any(DriverVehicle.class));
    }

    /**
     * 测试移除常用车辆（关联不存在）
     */
    @Test
    void testRemoveCommonVehicleNotFound() {
        when(driverVehicleMapper.selectByDriverAndVehicle(1L, 999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            driverService.removeCommonVehicle(1L, 999L);
        });

        verify(driverVehicleMapper, times(1)).selectByDriverAndVehicle(1L, 999L);
    }

    /**
     * 测试获取常用车辆列表
     */
    @Test
    void testGetCommonVehicles() {
        when(driverVehicleMapper.selectByDriverId(1L)).thenReturn(Collections.singletonList(testDriverVehicle));

        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleNo", "京A12345");
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);

        List<DriverVehicleVO> result = driverService.getCommonVehicles(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getVehicleId());
        assertTrue(result.get(0).getIsDefault());
        verify(driverVehicleMapper, times(1)).selectByDriverId(1L);
    }

    /**
     * 测试获取司机ID列表
     */
    @Test
    void testGetDriverIds() {
        List<Driver> drivers = Collections.singletonList(testDriver);
        when(driverMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drivers);

        List<Long> result = driverService.getDriverIds();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0));
        verify(driverMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试增加车辆使用次数
     */
    @Test
    void testIncrementVehicleUseCount() {
        when(driverVehicleMapper.incrementUseCount(1L, 100L)).thenReturn(1);

        driverService.incrementVehicleUseCount(1L, 100L);

        verify(driverVehicleMapper, times(1)).incrementUseCount(1L, 100L);
    }

    /**
     * 测试根据用户ID添加常用车辆
     */
    @Test
    void testAddCommonVehicleByUserId() {
        User testUser = new User();
        testUser.setId(100L);
        testUser.setRoleId(1L);
        when(userMapper.selectById(100L)).thenReturn(testUser);
        
        Role driverRole = new Role();
        driverRole.setId(1L);
        driverRole.setRoleCode("DRIVER");
        when(roleMapper.selectById(1L)).thenReturn(driverRole);
        
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleType", 1);
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);
        
        when(driverVehicleMapper.selectByDriverAndVehicle(100L, 100L)).thenReturn(null);
        when(driverVehicleMapper.selectByDriverId(100L)).thenReturn(Collections.emptyList());
        when(driverVehicleMapper.insert(any(DriverVehicle.class))).thenReturn(1);

        driverService.addCommonVehicleByUserId(100L, 100L);

        verify(userMapper, times(1)).selectById(100L);
        verify(driverVehicleMapper, times(1)).insert(any(DriverVehicle.class));
    }

    /**
     * 测试根据用户ID设置默认车辆
     */
    @Test
    void testSetDefaultVehicleByUserId() {
        when(driverVehicleMapper.selectByDriverAndVehicle(100L, 100L)).thenReturn(testDriverVehicle);
        when(driverVehicleMapper.clearDefaultByDriverId(100L)).thenReturn(1);
        when(driverVehicleMapper.updateById(any(DriverVehicle.class))).thenReturn(1);

        driverService.setDefaultVehicleByUserId(100L, 100L);

        verify(driverVehicleMapper, times(1)).clearDefaultByDriverId(100L);
    }

    /**
     * 测试根据用户ID获取常用车辆
     */
    @Test
    void testGetCommonVehiclesByUserId() {
        when(driverVehicleMapper.selectByDriverId(100L)).thenReturn(Collections.singletonList(testDriverVehicle));

        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 100L);
        vehicleMap.put("vehicleNo", "京A12345");
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(100L)).thenReturn(vehicleResult);

        List<DriverVehicleVO> result = driverService.getCommonVehiclesByUserId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试获取可用维修工
     */
    @Test
    void testGetAvailableRepairmen() {
        Repairman repairman = new Repairman();
        repairman.setId(1L);
        repairman.setUserId(200L);
        repairman.setRepairmanName("测试维修工");
        repairman.setStatus(1);
        repairman.setGender(1);
        repairman.setIdCard("110101199001011235");
        repairman.setCreateTime(LocalDateTime.now());
        repairman.setDeleted(0);

        when(repairmanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(repairman));
        when(driverMapper.hasRole(200L, RoleEnum.REPAIRMAN.getValue())).thenReturn(true);

        List<DriverVO> result = driverService.getAvailableRepairmen();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试维修工", result.get(0).getDriverName());
    }

    /**
     * 测试获取可用安全员
     */
    @Test
    void testGetAvailableSafetyOfficers() {
        SafetyOfficer safetyOfficer = new SafetyOfficer();
        safetyOfficer.setId(1L);
        safetyOfficer.setUserId(300L);
        safetyOfficer.setOfficerName("测试安全员");
        safetyOfficer.setStatus(1);
        safetyOfficer.setGender(1);
        safetyOfficer.setIdCard("110101199001011236");
        safetyOfficer.setCreateTime(LocalDateTime.now());
        safetyOfficer.setDeleted(0);

        when(safetyOfficerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(safetyOfficer));
        when(driverMapper.hasRole(300L, RoleEnum.SAFETY_OFFICER.getValue())).thenReturn(true);

        List<DriverVO> result = driverService.getAvailableSafetyOfficers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试安全员", result.get(0).getDriverName());
    }
}
