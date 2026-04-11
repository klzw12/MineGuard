package com.klzw.service.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klzw.service.user.service.DriverService;
import com.klzw.service.user.vo.DriverVehicleVO;
import com.klzw.service.user.vo.DriverVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DriverController切片测试类
 * 使用MockMvc独立测试，不加载Spring上下文
 */
@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private DriverVO testDriverVO;
    private DriverVehicleVO testDriverVehicleVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(driverController).build();
        
        testDriverVO = new DriverVO();
        testDriverVO.setId(1L);
        testDriverVO.setUserId(100L);
        testDriverVO.setDriverName("测试司机");
        testDriverVO.setGender(1);
        testDriverVO.setLicenseType("A1");
        testDriverVO.setDrivingYears(10);
        testDriverVO.setStatus(1);
        testDriverVO.setScore(85);
        testDriverVO.setCreateTime(LocalDateTime.now());

        testDriverVehicleVO = new DriverVehicleVO();
        testDriverVehicleVO.setId(1L);
        testDriverVehicleVO.setDriverId(1L);
        testDriverVehicleVO.setVehicleId(100L);
        testDriverVehicleVO.setVehicleNo("京A12345");
        testDriverVehicleVO.setUseCount(5);
        testDriverVehicleVO.setIsDefault(true);
    }

    @AfterEach
    void tearDown() {
        reset(driverService);
    }

    @Test
    void testGetById() throws Exception {
        when(driverService.getById(1L)).thenReturn(testDriverVO);

        mockMvc.perform(get("/user/driver/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.driverName").value("测试司机"));

        verify(driverService, times(1)).getById(1L);
    }

    @Test
    void testGetByIdNotFound() throws Exception {
        when(driverService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/user/driver/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(driverService, times(1)).getById(999L);
    }

    @Test
    void testGetByUserId() throws Exception {
        when(driverService.getByUserId(100L)).thenReturn(testDriverVO);

        mockMvc.perform(get("/user/driver/user/{userId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(100));

        verify(driverService, times(1)).getByUserId(100L);
    }

    @Test
    void testGetList() throws Exception {
        when(driverService.getList("测试", 1)).thenReturn(Collections.singletonList(testDriverVO));

        mockMvc.perform(get("/user/driver/list")
                        .param("driverName", "测试")
                        .param("status", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].driverName").value("测试司机"));

        verify(driverService, times(1)).getList("测试", 1);
    }

    @Test
    void testGetListWithoutParams() throws Exception {
        when(driverService.getList(null, null)).thenReturn(Collections.singletonList(testDriverVO));

        mockMvc.perform(get("/user/driver/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).getList(null, null);
    }

    @Test
    void testUpdateStatus() throws Exception {
        doNothing().when(driverService).updateStatus(1L, 1);

        mockMvc.perform(put("/user/driver/{driverId}/status", 1L)
                        .param("status", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).updateStatus(1L, 1);
    }

    @Test
    void testGetAvailableDrivers() throws Exception {
        when(driverService.getAvailableDrivers()).thenReturn(Collections.singletonList(testDriverVO));

        mockMvc.perform(get("/user/driver/available")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].driverName").value("测试司机"));

        verify(driverService, times(1)).getAvailableDrivers();
    }

    @Test
    void testGetAvailableRepairmen() throws Exception {
        when(driverService.getAvailableRepairmen()).thenReturn(Collections.singletonList(testDriverVO));

        mockMvc.perform(get("/user/driver/available-repairmen")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].driverName").value("测试司机"));

        verify(driverService, times(1)).getAvailableRepairmen();
    }

    @Test
    void testGetAvailableSafetyOfficers() throws Exception {
        when(driverService.getAvailableSafetyOfficers()).thenReturn(Collections.singletonList(testDriverVO));

        mockMvc.perform(get("/user/driver/available-safety-officers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].driverName").value("测试司机"));

        verify(driverService, times(1)).getAvailableSafetyOfficers();
    }

    @Test
    void testSelectBestDriver() throws Exception {
        when(driverService.selectBestDriver(null, null)).thenReturn(testDriverVO);

        mockMvc.perform(post("/user/driver/best")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.driverName").value("测试司机"));

        verify(driverService, times(1)).selectBestDriver(null, null);
    }

    @Test
    void testAddCommonVehicleByUserId() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("vehicleId", 100L);

        doNothing().when(driverService).addCommonVehicleByUserId(100L, 100L);

        mockMvc.perform(post("/user/driver/user/{userId}/common-vehicle", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).addCommonVehicleByUserId(100L, 100L);
    }

    @Test
    void testRemoveCommonVehicleByUserId() throws Exception {
        doNothing().when(driverService).removeCommonVehicleByUserId(100L, 100L);

        mockMvc.perform(delete("/user/driver/user/{userId}/common-vehicle/{vehicleId}", 100L, 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).removeCommonVehicleByUserId(100L, 100L);
    }

    @Test
    void testGetCommonVehiclesByUserId() throws Exception {
        when(driverService.getCommonVehiclesByUserId(100L)).thenReturn(Collections.singletonList(testDriverVehicleVO));

        mockMvc.perform(get("/user/driver/user/{userId}/common-vehicles", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].vehicleId").value(100));

        verify(driverService, times(1)).getCommonVehiclesByUserId(100L);
    }

    @Test
    void testAddCommonVehicle() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("vehicleId", 100L);

        doNothing().when(driverService).addCommonVehicle(1L, 100L);

        mockMvc.perform(post("/user/driver/{driverId}/common-vehicle", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).addCommonVehicle(1L, 100L);
    }

    @Test
    void testRemoveCommonVehicle() throws Exception {
        doNothing().when(driverService).removeCommonVehicle(1L, 100L);

        mockMvc.perform(delete("/user/driver/{driverId}/common-vehicle/{vehicleId}", 1L, 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).removeCommonVehicle(1L, 100L);
    }

    @Test
    void testSetDefaultVehicle() throws Exception {
        doNothing().when(driverService).setDefaultVehicle(1L, 100L);

        mockMvc.perform(put("/user/driver/{driverId}/common-vehicle/{vehicleId}/default", 1L, 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(driverService, times(1)).setDefaultVehicle(1L, 100L);
    }

    @Test
    void testGetCommonVehicles() throws Exception {
        when(driverService.getCommonVehicles(1L)).thenReturn(Collections.singletonList(testDriverVehicleVO));

        mockMvc.perform(get("/user/driver/{driverId}/common-vehicles", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].vehicleId").value(100));

        verify(driverService, times(1)).getCommonVehicles(1L);
    }

    @Test
    void testGetDriverIds() throws Exception {
        when(driverService.getDriverIds()).thenReturn(Collections.singletonList(1L));

        mockMvc.perform(get("/user/driver/ids")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value(1));

        verify(driverService, times(1)).getDriverIds();
    }
}
