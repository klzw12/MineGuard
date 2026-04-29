package com.klzw.service.vehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.vo.BestVehicleVO;
import com.klzw.service.vehicle.vo.VehicleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VehicleController切片测试类
 */
@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private ObjectMapper objectMapper;
    private Vehicle testVehicle;
    private VehicleVO testVehicleVO;
    private BestVehicleVO testBestVehicleVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化测试车辆数据
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setVehicleNo("京A12345");
        testVehicle.setVehicleType(1);
        testVehicle.setBrand("特斯拉");
        testVehicle.setModel("Model 3");
        testVehicle.setStatus(VehicleStatusEnum.IDLE.getCode());
        testVehicle.setFuelLevel(80);
        testVehicle.setCreateTime(LocalDateTime.now());
        testVehicle.setUpdateTime(LocalDateTime.now());

        // 初始化测试VO
        testVehicleVO = new VehicleVO();
        testVehicleVO.setId("1");
        testVehicleVO.setVehicleNo("京A12345");
        testVehicleVO.setVehicleType(1);
        testVehicleVO.setBrand("特斯拉");
        testVehicleVO.setModel("Model 3");
        testVehicleVO.setStatus(0);
        testVehicleVO.setFuelLevel(80);

        // 初始化最佳车辆VO
        testBestVehicleVO = new BestVehicleVO();
        testBestVehicleVO.setId(1L);
        testBestVehicleVO.setVehicleNo("京A12345");
        testBestVehicleVO.setVehicleType(1);
        testBestVehicleVO.setBrand("特斯拉");
        testBestVehicleVO.setModel("Model 3");
        testBestVehicleVO.setFuelLevel(80);
        testBestVehicleVO.setScore(100);
        testBestVehicleVO.setReason("油量充足");
    }

    /**
     * 测试创建车辆
     */
    @Test
    void testCreateVehicle() throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class))).thenReturn(testVehicle);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vehicleNo", "京A12345");
        requestBody.put("vehicleType", 1);
        requestBody.put("brand", "特斯拉");
        requestBody.put("model", "Model 3");

        mockMvc.perform(post("/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.vehicleNo").value("京A12345"));
    }

    /**
     * 测试更新车辆
     */
    @Test
    void testUpdateVehicle() throws Exception {
        when(vehicleService.updateVehicle(anyLong(), any(Vehicle.class))).thenReturn(testVehicle);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("brand", "特斯拉更新");
        requestBody.put("model", "Model Y");

        mockMvc.perform(put("/vehicle/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试删除车辆
     */
    @Test
    void testDeleteVehicle() throws Exception {
        when(vehicleService.deleteVehicle(1L)).thenReturn(true);

        mockMvc.perform(delete("/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试获取车辆详情
     */
    @Test
    void testGetVehicleById() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(testVehicleVO);

        mockMvc.perform(get("/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.vehicleNo").value("京A12345"));
    }

    /**
     * 测试获取不存在的车辆详情
     */
    @Test
    void testGetVehicleByIdNotFound() throws Exception {
        when(vehicleService.getVehicleById(999L)).thenReturn(null);

        mockMvc.perform(get("/vehicle/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /**
     * 测试分页查询车辆
     */
    @Test
    void testGetVehiclePage() throws Exception {
        when(vehicleService.getVehiclePage(1, 10, null, null))
                .thenReturn(Collections.singletonList(testVehicleVO));

        mockMvc.perform(get("/vehicle/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试分页查询带条件
     */
    @Test
    void testGetVehiclePageWithCondition() throws Exception {
        when(vehicleService.getVehiclePage(1, 10, "京A", 0))
                .thenReturn(Collections.singletonList(testVehicleVO));

        mockMvc.perform(get("/vehicle/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("vehicleNo", "京A")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试上传车辆照片
     */
    @Test
    void testUploadVehiclePhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(vehicleService.uploadVehiclePhoto(anyLong(), any())).thenReturn("http://test.com/photo.jpg");

        mockMvc.perform(multipart("/vehicle/1/photo")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("http://test.com/photo.jpg"));
    }

    /**
     * 测试上传行驶证并进行OCR识别
     */
    @Test
    void testUploadLicenseAndOCR() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "license.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test license content".getBytes()
        );

        when(vehicleService.uploadLicenseAndOCR(anyLong(), any())).thenReturn(testVehicle);

        mockMvc.perform(multipart("/vehicle/1/license")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试选择最佳车辆
     */
    @Test
    void testSelectBestVehicles() throws Exception {
        when(vehicleService.selectBestVehicles(any(BestVehicleQueryDTO.class)))
                .thenReturn(Collections.singletonList(testBestVehicleVO));

        mockMvc.perform(post("/vehicle/best")
                        .param("cargoWeight", "0.5")
                        .param("driverId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取所有可用车辆
     */
    @Test
    void testGetAvailableVehicles() throws Exception {
        when(vehicleService.getAvailableVehicles())
                .thenReturn(Collections.singletonList(testVehicleVO));

        mockMvc.perform(get("/vehicle/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试报废车辆
     */
    @Test
    void testScrapVehicle() throws Exception {
        when(vehicleService.scrapVehicle(1L)).thenReturn(true);

        mockMvc.perform(put("/vehicle/1/scrap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试检查车辆是否存在
     */
    @Test
    void testExistsById() throws Exception {
        when(vehicleService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/vehicle/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试检查车辆不存在
     */
    @Test
    void testExistsByIdNotFound() throws Exception {
        when(vehicleService.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/vehicle/999/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    /**
     * 测试更新车辆维修状态
     */
    @Test
    void testUpdateMaintenanceStatus() throws Exception {
        when(vehicleService.updateMaintenanceStatus(1L, 2)).thenReturn(testVehicle);

        mockMvc.perform(put("/vehicle/1/maintenance")
                        .param("maintenanceStatus", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试上传车辆保险信息
     */
    @Test
    void testUploadInsuranceInfo() throws Exception {
        when(vehicleService.uploadInsuranceInfo(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testVehicle);

        mockMvc.perform(post("/vehicle/1/insurance")
                        .param("insuranceCompany", "平安保险")
                        .param("policyNo", "POL123456")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
