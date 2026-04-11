package com.klzw.service.dispatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.service.dispatch.controller.DispatchController;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.service.DispatchService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DispatchController切片测试类
 */
@ExtendWith(MockitoExtension.class)
class DispatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private DispatchController dispatchController;

    private ObjectMapper objectMapper;
    private TransportTask testTask;
    private DispatchTaskVO testTaskVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dispatchController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testTask = new TransportTask();
        testTask.setId(1L);
        testTask.setTaskNo("TRANS20260409001");
        testTask.setPlanId(1L);
        testTask.setVehicleId(1L);
        testTask.setExecutorId(1L);
        testTask.setStatus(0);
        testTask.setPriority("normal");
        testTask.setStartLocation("北京");
        testTask.setEndLocation("上海");
        testTask.setCargoWeight(new BigDecimal("100"));
        testTask.setScheduledStartTime(LocalDateTime.now());
        testTask.setScheduledEndTime(LocalDateTime.now().plusHours(8));
        testTask.setCreateTime(LocalDateTime.now());
        testTask.setUpdateTime(LocalDateTime.now());

        testTaskVO = new DispatchTaskVO();
        testTaskVO.setId(1L);
        testTaskVO.setTaskNo("TRANS20260409001");
        testTaskVO.setPlanId(1L);
        testTaskVO.setVehicleId(1L);
        testTaskVO.setDriverId(1L);
        testTaskVO.setExecutorId(1L);
        testTaskVO.setStatus(0);
        testTaskVO.setStatusName("待接单");
        testTaskVO.setPriority("normal");
        testTaskVO.setStartLocation("北京");
        testTaskVO.setEndLocation("上海");
        testTaskVO.setCargoWeight(new BigDecimal("100"));
    }

    /**
     * 测试创建调度任务
     */
    @Test
    void testCreateDispatchTask() throws Exception {
        when(dispatchService.createDispatchTask(any(TransportTask.class))).thenReturn(testTask);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("planId", 1L);
        requestBody.put("vehicleId", 1L);
        requestBody.put("executorId", 1L);
        requestBody.put("startLocation", "北京");
        requestBody.put("endLocation", "上海");
        requestBody.put("cargoWeight", 100);

        mockMvc.perform(post("/dispatch/main/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskNo").value("TRANS20260409001"));
    }

    /**
     * 测试更新调度任务
     */
    @Test
    void testUpdateDispatchTask() throws Exception {
        when(dispatchService.updateDispatchTask(any(TransportTask.class))).thenReturn(testTask);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("remark", "更新备注");

        mockMvc.perform(put("/dispatch/main/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试删除调度任务
     */
    @Test
    void testDeleteDispatchTask() throws Exception {
        when(dispatchService.deleteDispatchTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/dispatch/main/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试获取调度任务详情
     */
    @Test
    void testGetDispatchTask() throws Exception {
        when(dispatchService.getDispatchTask(1L)).thenReturn(testTask);

        mockMvc.perform(get("/dispatch/main/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskNo").value("TRANS20260409001"));
    }

    /**
     * 测试获取调度任务列表
     */
    @Test
    void testGetDispatchTaskList() throws Exception {
        when(dispatchService.getAllTaskList(any(), any(), any()))
                .thenReturn(Collections.singletonList(testTaskVO));

        mockMvc.perform(get("/dispatch/main/task/list")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取计划下的调度任务
     */
    @Test
    void testGetDispatchTasksByPlanId() throws Exception {
        when(dispatchService.getTasksByPlanId(1L))
                .thenReturn(Collections.singletonList(testTaskVO));

        mockMvc.perform(get("/dispatch/main/task/plan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取车辆待处理任务
     */
    @Test
    void testGetPendingTasksByVehicle() throws Exception {
        when(dispatchService.getPendingTasksByVehicle(1L))
                .thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(get("/dispatch/main/task/vehicle/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取司机待处理任务
     */
    @Test
    void testGetPendingTasksByDriver() throws Exception {
        when(dispatchService.getPendingTasksByDriver(1L))
                .thenReturn(Collections.singletonList(testTaskVO));

        mockMvc.perform(get("/dispatch/main/task/driver/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试执行调度任务
     */
    @Test
    void testExecuteDispatch() throws Exception {
        when(dispatchService.executeDispatch(1L)).thenReturn(true);

        mockMvc.perform(post("/dispatch/main/task/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试取消调度任务
     */
    @Test
    void testCancelDispatchTask() throws Exception {
        when(dispatchService.cancelDispatchTask(1L)).thenReturn(true);

        mockMvc.perform(post("/dispatch/main/task/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试重新调度已取消的任务
     */
    @Test
    void testRescheduleTask() throws Exception {
        when(dispatchService.rescheduleTask(1L)).thenReturn(true);

        mockMvc.perform(post("/dispatch/main/task/1/reschedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 测试分配车辆
     */
    @Test
    void testAssignVehicle() throws Exception {
        doNothing().when(dispatchService).assignVehicle(1L, 1L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vehicleId", 1L);

        mockMvc.perform(put("/dispatch/main/task/1/assign-vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试分配司机
     */
    @Test
    void testAssignDriver() throws Exception {
        doNothing().when(dispatchService).assignDriver(1L, 1L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("driverId", 1L);

        mockMvc.perform(put("/dispatch/main/task/1/assign-driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试开始任务
     */
    @Test
    void testStartTask() throws Exception {
        doNothing().when(dispatchService).updateTaskStatusToInProgress(1L);

        mockMvc.perform(put("/dispatch/main/task/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试完成任务
     */
    @Test
    void testCompleteTask() throws Exception {
        doNothing().when(dispatchService).completeTask(1L);

        mockMvc.perform(put("/dispatch/main/task/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试重新分配任务
     */
    @Test
    void testReassignTask() throws Exception {
        doNothing().when(dispatchService).reassignTask(1L, 2L, 2L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("newVehicleId", 2L);
        requestBody.put("newDriverId", 2L);

        mockMvc.perform(put("/dispatch/main/task/1/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取可重新分配的任务
     */
    @Test
    void testGetAvailableTasksForReassignment() throws Exception {
        when(dispatchService.getAvailableTasksForReassignment(any(), any()))
                .thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(get("/dispatch/main/task/available")
                        .param("startTime", "2024-04-09 08:00:00")
                        .param("endTime", "2024-04-09 18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试车辆故障动态调整
     */
    @Test
    void testDynamicAdjustVehicleFault() throws Exception {
        doNothing().when(dispatchService).dynamicAdjustForVehicleFault(1L);

        mockMvc.perform(post("/dispatch/main/task/dynamic-adjust/vehicle-fault/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试司机请假动态调整
     */
    @Test
    void testDynamicAdjustDriverLeave() throws Exception {
        doNothing().when(dispatchService).dynamicAdjustForDriverLeave(1L);

        mockMvc.perform(post("/dispatch/main/task/dynamic-adjust/driver-leave/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试线路堵塞动态调整
     */
    @Test
    void testDynamicAdjustRouteBlock() throws Exception {
        doNothing().when(dispatchService).dynamicAdjustForRouteBlock(1L);

        mockMvc.perform(post("/dispatch/main/task/dynamic-adjust/route-block/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试用户请假任务重新分配
     */
    @Test
    void testReassignTasksByUserLeave() throws Exception {
        doNothing().when(dispatchService).reassignTasksByUserLeave(1L, "DRIVER");

        mockMvc.perform(post("/dispatch/main/task/dynamic-adjust/user-leave")
                        .param("userId", "1")
                        .param("roleCode", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试从故障申报创建维修任务
     */
    @Test
    void testCreateMaintenanceTaskFromFault() throws Exception {
        when(dispatchService.createMaintenanceTaskFromFault(anyMap())).thenReturn(1L);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vehicleId", 1);
        requestBody.put("faultType", "发动机");
        requestBody.put("faultDescription", "发动机无法启动");
        requestBody.put("severity", 2);

        mockMvc.perform(post("/dispatch/main/maintenance-task/from-fault")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }
}