package com.klzw.service.warning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.WarningRecordService;
import com.klzw.service.warning.service.WarningRuleService;
import com.klzw.service.warning.vo.WarningRecordVO;
import com.klzw.service.warning.vo.WarningRuleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WarningController切片测试类
 */
@ExtendWith(MockitoExtension.class)
class WarningControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WarningRecordService warningRecordService;

    @Mock
    private WarningRuleService warningRuleService;

    @Mock
    private WarningTriggerProcessor warningTriggerProcessor;

    @InjectMocks
    private WarningController warningController;

    private ObjectMapper objectMapper;
    private WarningRecordVO testRecordVO;
    private WarningRuleVO testRuleVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(warningController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化测试数据
        testRecordVO = new WarningRecordVO();
        testRecordVO.setId(1L);
        testRecordVO.setWarningNo("WARN202604090001");
        testRecordVO.setRuleId(1L);
        testRecordVO.setRuleName("车辆故障预警规则");
        testRecordVO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecordVO.setWarningTypeName("车辆故障");
        testRecordVO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecordVO.setWarningLevelName("高风险");
        testRecordVO.setVehicleId(1L);
        testRecordVO.setVehicleNo("京A12345");
        testRecordVO.setDriverId(1L);
        testRecordVO.setDriverName("张三");
        testRecordVO.setTripId(1L);
        testRecordVO.setTripNo("TRIP202604090001");
        testRecordVO.setLongitude(116.404);
        testRecordVO.setLatitude(39.915);
        testRecordVO.setSpeed(new BigDecimal("60"));
        testRecordVO.setWarningContent("测试预警内容");
        testRecordVO.setWarningTime(LocalDateTime.now());
        testRecordVO.setStatus(0);
        testRecordVO.setStatusName("待处理");
        testRecordVO.setCreateTime(LocalDateTime.now());

        testRuleVO = new WarningRuleVO();
        testRuleVO.setId(1L);
        testRuleVO.setRuleName("车辆故障预警规则");
        testRuleVO.setRuleCode("VEHICLE_FAULT_001");
        testRuleVO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRuleVO.setWarningTypeName("车辆故障");
        testRuleVO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRuleVO.setWarningLevelName("高风险");
        testRuleVO.setThresholdValue("{\"temperature\": 90}");
        testRuleVO.setStatus(1);
        testRuleVO.setDescription("车辆故障预警规则描述");
        testRuleVO.setCreateTime(LocalDateTime.now());
    }

    /**
     * 测试分页查询预警记录
     */
    @Test
    void testPageWarningRecords() throws Exception {
        PageResult<WarningRecordVO> pageResult = PageResult.of(1L, 1, 10, Collections.singletonList(testRecordVO));
        when(warningRecordService.page(any(PageRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/warning/record/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    /**
     * 测试获取预警记录详情
     */
    @Test
    void testGetWarningRecordById() throws Exception {
        when(warningRecordService.getById(1L)).thenReturn(testRecordVO);

        mockMvc.perform(get("/warning/record/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.warningNo").value("WARN202604090001"));
    }

    /**
     * 测试根据行程ID查询预警记录
     */
    @Test
    void testGetWarningRecordsByTripId() throws Exception {
        when(warningRecordService.getByTripId(1L)).thenReturn(Collections.singletonList(testRecordVO));

        mockMvc.perform(get("/warning/record/trip")
                        .param("tripId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试创建预警记录
     */
    @Test
    void testCreateWarningRecord() throws Exception {
        when(warningRecordService.createWarning(any(WarningRecordDTO.class))).thenReturn(1L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("warningType", 1);
        requestBody.put("warningLevel", 3);
        requestBody.put("vehicleId", 1);
        requestBody.put("warningContent", "测试预警内容");

        mockMvc.perform(post("/warning/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    /**
     * 测试处理预警记录
     */
    @Test
    void testHandleWarning() throws Exception {
        doNothing().when(warningRecordService).handleWarning(anyLong(), any(WarningHandleDTO.class));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("handleResult", "已处理");
        requestBody.put("status", 2);

        mockMvc.perform(put("/warning/record/1/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试忽略预警记录
     */
    @Test
    void testIgnoreWarning() throws Exception {
        doNothing().when(warningRecordService).ignoreWarning(1L);

        mockMvc.perform(put("/warning/record/1/ignore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试处理事件触发
     */
    @Test
    void testProcessEventTrigger() throws Exception {
        WarningRecord testRecord = new WarningRecord();
        testRecord.setId(1L);
        testRecord.setWarningNo("WARN202604090001");
        testRecord.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecord.setVehicleId(1L);
        testRecord.setWarningContent("测试预警内容");

        when(warningRecordService.processEventTrigger(any(WarningTrackDTO.class), anyString()))
                .thenReturn(testRecord);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> track = new HashMap<>();
        track.put("vehicleId", 1);
        track.put("longitude", 116.404);
        track.put("latitude", 39.915);
        requestBody.put("track", track);
        requestBody.put("eventType", "VEHICLE_FAULT");

        mockMvc.perform(post("/warning/process-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试处理轨迹点
     */
    @Test
    void testProcessTrack() throws Exception {
        WarningRecord testRecord = new WarningRecord();
        testRecord.setId(1L);
        testRecord.setWarningNo("WARN202604090001");
        testRecord.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecord.setVehicleId(1L);
        testRecord.setWarningContent("测试预警内容");

        when(warningRecordService.processWarningTrack(any(WarningTrackDTO.class)))
                .thenReturn(testRecord);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vehicleId", 1);
        requestBody.put("longitude", 116.404);
        requestBody.put("latitude", 39.915);

        mockMvc.perform(post("/warning/process-track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取预警统计
     */
    @Test
    void testGetWarningStatistics() throws Exception {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", 100);
        statistics.put("lowLevelCount", 30);
        statistics.put("mediumLevelCount", 50);
        statistics.put("highLevelCount", 20);
        statistics.put("pendingCount", 40);
        statistics.put("handledCount", 60);

        when(warningRecordService.getWarningStatistics(anyString(), anyString()))
                .thenReturn(statistics);

        mockMvc.perform(get("/warning/statistics")
                        .param("startTime", "2024-04-01 00:00:00")
                        .param("endTime", "2024-04-30 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(100));
    }

    /**
     * 测试获取预警趋势
     */
    @Test
    void testGetWarningTrend() throws Exception {
        List<Map<String, Object>> trend = Collections.singletonList(
                Map.of("date", "2024-04-09", "count", 10)
        );

        when(warningRecordService.getWarningTrend(anyInt())).thenReturn(trend);

        mockMvc.perform(get("/warning/trend")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取预警类型统计
     */
    @Test
    void testGetWarningTypeStatistics() throws Exception {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("车辆故障", 20);
        statistics.put("路线偏离", 15);
        statistics.put("长时间停留", 10);

        when(warningRecordService.getWarningTypeStatistics(anyString(), anyString()))
                .thenReturn(statistics);

        mockMvc.perform(get("/warning/statistics/type")
                        .param("startTime", "2024-04-01 00:00:00")
                        .param("endTime", "2024-04-30 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取预警级别统计
     */
    @Test
    void testGetWarningLevelStatistics() throws Exception {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("低风险", 30);
        statistics.put("中风险", 50);
        statistics.put("高风险", 20);

        when(warningRecordService.getWarningLevelStatistics(anyString(), anyString()))
                .thenReturn(statistics);

        mockMvc.perform(get("/warning/statistics/level")
                        .param("startTime", "2024-04-01 00:00:00")
                        .param("endTime", "2024-04-30 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试标记电话未接
     */
    @Test
    void testMarkPhoneUnanswered() throws Exception {
        doNothing().when(warningTriggerProcessor).markPhoneUnanswered(1L);

        mockMvc.perform(post("/warning/phone/unanswered")
                        .param("vehicleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试清除电话未接标记
     */
    @Test
    void testClearPhoneUnanswered() throws Exception {
        doNothing().when(warningTriggerProcessor).clearPhoneUnanswered(1L);

        mockMvc.perform(delete("/warning/phone/unanswered/clear")
                        .param("vehicleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试标记车辆安全区域状态
     */
    @Test
    void testMarkVehicleInSafeZone() throws Exception {
        doNothing().when(warningTriggerProcessor).markVehicleInSafeZone(1L, true);

        mockMvc.perform(post("/warning/safe-zone/mark")
                        .param("vehicleId", "1")
                        .param("inSafeZone", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试分页查询预警规则
     */
    @Test
    void testPageWarningRules() throws Exception {
        PageResult<WarningRuleVO> pageResult = PageResult.of(1L, 1, 10, Collections.singletonList(testRuleVO));
        when(warningRuleService.page(any(PageRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/warning/rule/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    /**
     * 测试获取预警规则详情
     */
    @Test
    void testGetWarningRuleById() throws Exception {
        when(warningRuleService.getById(1L)).thenReturn(testRuleVO);

        mockMvc.perform(get("/warning/rule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("VEHICLE_FAULT_001"));
    }

    /**
     * 测试根据编码获取预警规则
     */
    @Test
    void testGetWarningRuleByCode() throws Exception {
        when(warningRuleService.getByCode("VEHICLE_FAULT_001")).thenReturn(testRuleVO);

        mockMvc.perform(get("/warning/rule/code/VEHICLE_FAULT_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("VEHICLE_FAULT_001"));
    }

    /**
     * 测试创建预警规则
     */
    @Test
    void testCreateWarningRule() throws Exception {
        when(warningRuleService.create(any(WarningRuleDTO.class))).thenReturn(1L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ruleName", "车辆故障预警规则");
        requestBody.put("ruleCode", "VEHICLE_FAULT_001");
        requestBody.put("warningType", 1);
        requestBody.put("warningLevel", 3);

        mockMvc.perform(post("/warning/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    /**
     * 测试更新预警规则
     */
    @Test
    void testUpdateWarningRule() throws Exception {
        doNothing().when(warningRuleService).update(anyLong(), any(WarningRuleDTO.class));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ruleName", "更新后的规则名称");
        requestBody.put("ruleCode", "VEHICLE_FAULT_001");
        requestBody.put("warningType", 1);
        requestBody.put("warningLevel", 3);

        mockMvc.perform(put("/warning/rule/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试删除预警规则
     */
    @Test
    void testDeleteWarningRule() throws Exception {
        doNothing().when(warningRuleService).delete(1L);

        mockMvc.perform(delete("/warning/rule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取所有启用的预警规则
     */
    @Test
    void testListAllRules() throws Exception {
        when(warningRuleService.listAll()).thenReturn(Collections.singletonList(testRuleVO));

        mockMvc.perform(get("/warning/rule/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试启用预警规则
     */
    @Test
    void testEnableRule() throws Exception {
        doNothing().when(warningRuleService).enable(1L);

        mockMvc.perform(put("/warning/rule/1/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试禁用预警规则
     */
    @Test
    void testDisableRule() throws Exception {
        doNothing().when(warningRuleService).disable(1L);

        mockMvc.perform(put("/warning/rule/1/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
