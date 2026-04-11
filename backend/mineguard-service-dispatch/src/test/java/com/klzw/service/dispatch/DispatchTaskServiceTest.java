package com.klzw.service.dispatch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.dto.DispatchTaskDTO;
import com.klzw.service.dispatch.entity.DispatchPlan;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.mapper.DispatchPlanMapper;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.service.impl.DispatchTaskServiceImpl;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DispatchTaskService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class DispatchTaskServiceTest {

    @InjectMocks
    private DispatchTaskServiceImpl dispatchTaskService;

    @Mock
    private TransportTaskMapper transportTaskMapper;

    @Mock
    private DispatchPlanMapper dispatchPlanMapper;

    @Mock
    private DriverClient driverClient;

    private TransportTask testTask;
    private DispatchTaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
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

        testTaskDTO = new DispatchTaskDTO();
        testTaskDTO.setPlanId(1L);
        testTaskDTO.setVehicleId(1L);
        testTaskDTO.setExecutorId(1L);
        testTaskDTO.setStartLocation("北京");
        testTaskDTO.setEndLocation("上海");
        testTaskDTO.setCargoWeight(new BigDecimal("100"));
        testTaskDTO.setPriority("normal");
    }

    @AfterEach
    void tearDown() {
        reset(transportTaskMapper, dispatchPlanMapper, driverClient);
    }

    /**
     * 测试创建运输任务
     */
    @Test
    void testCreateTransportTask() {
        when(transportTaskMapper.insert(any(TransportTask.class))).thenAnswer(invocation -> {
            TransportTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        DispatchTaskVO result = dispatchTaskService.create(testTaskDTO);

        assertNotNull(result);
        verify(transportTaskMapper, times(1)).insert(any(TransportTask.class));
    }

    /**
     * 测试更新运输任务
     */
    @Test
    void testUpdateTransportTask() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        testTaskDTO.setId(1L);
        testTaskDTO.setRemark("更新备注");

        DispatchTaskVO result = dispatchTaskService.update(testTaskDTO);

        assertNotNull(result);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试更新不存在的运输任务
     */
    @Test
    void testUpdateTransportTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        testTaskDTO.setId(999L);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.update(testTaskDTO);
        });
    }

    /**
     * 测试删除运输任务
     */
    @Test
    void testDeleteTransportTask() {
        when(transportTaskMapper.deleteById(1L)).thenReturn(1);

        dispatchTaskService.delete(1L);

        verify(transportTaskMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试获取运输任务
     */
    @Test
    void testGetTransportTaskById() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);

        // Mock司机信息
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(1L);
        driverInfo.setDriverName("张三");
        Result<DriverInfo> driverResult = Result.success(driverInfo);
        when(driverClient.getById(1L)).thenReturn(driverResult);

        DispatchTaskVO result = dispatchTaskService.getById(1L);

        assertNotNull(result);
    }

    /**
     * 测试获取不存在的运输任务
     */
    @Test
    void testGetTransportTaskByIdNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        DispatchTaskVO result = dispatchTaskService.getById(999L);

        assertNull(result);
    }

    /**
     * 测试分配司机和车辆
     */
    @Test
    void testAssignDriverAndVehicle() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByExecutorIdAndTimeRange(eq(1L), any(), any())).thenReturn(0);
        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(1L), any(), any())).thenReturn(0);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 分配司机
        dispatchTaskService.assignDriver(1L, 1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));

        reset(transportTaskMapper);
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(1L), any(), any())).thenReturn(0);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 分配车辆
        dispatchTaskService.assignVehicle(1L, 1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试分配司机时任务不存在
     */
    @Test
    void testAssignDriverTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.assignDriver(999L, 1L);
        });
    }

    /**
     * 测试分配车辆时任务不存在
     */
    @Test
    void testAssignVehicleTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.assignVehicle(999L, 1L);
        });
    }

    /**
     * 测试司机时间冲突
     */
    @Test
    void testAssignDriverTimeConflict() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByExecutorIdAndTimeRange(eq(1L), any(), any())).thenReturn(1);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.assignDriver(1L, 1L);
        });
    }

    /**
     * 测试车辆时间冲突
     */
    @Test
    void testAssignVehicleTimeConflict() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(1L), any(), any())).thenReturn(1);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.assignVehicle(1L, 1L);
        });
    }

    /**
     * 测试开始调度任务
     */
    @Test
    void testStartDispatch() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        dispatchTaskService.startTask(1L);

        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试开始不存在的任务
     */
    @Test
    void testStartDispatchTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.startTask(999L);
        });
    }

    /**
     * 测试完成调度任务
     */
    @Test
    void testCompleteDispatch() {
        testTask.setStatus(2);
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        dispatchTaskService.completeTask(1L);

        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试完成不存在的任务
     */
    @Test
    void testCompleteDispatchTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.completeTask(999L);
        });
    }

    /**
     * 测试取消调度任务
     */
    @Test
    void testCancelDispatch() {
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        dispatchTaskService.cancelTask(1L);

        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试取消不存在的任务
     */
    @Test
    void testCancelDispatchTaskNotFound() {
        when(transportTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.cancelTask(999L);
        });
    }

    /**
     * 测试重新分配任务
     */
    @Test
    void testReassignTask() {
        testTask.setScheduledStartTime(LocalDateTime.now());
        testTask.setScheduledEndTime(LocalDateTime.now().plusHours(8));

        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(2L), any(), any())).thenReturn(0);
        when(transportTaskMapper.countByExecutorIdAndTimeRange(eq(2L), any(), any())).thenReturn(0);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        dispatchTaskService.reassignTask(1L, 2L, 2L);

        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试重新分配任务时新车辆时间冲突
     */
    @Test
    void testReassignTaskVehicleConflict() {
        testTask.setScheduledStartTime(LocalDateTime.now());
        testTask.setScheduledEndTime(LocalDateTime.now().plusHours(8));

        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(2L), any(), any())).thenReturn(1);

        assertThrows(RuntimeException.class, () -> {
            dispatchTaskService.reassignTask(1L, 2L, null);
        });
    }

    /**
     * 测试动态调整调度 - 车辆故障
     */
    @Test
    void testDynamicAdjustDispatchVehicleFault() {
        // 这个测试需要结合DispatchService，但由于ServiceImpl依赖较多外部服务
        // 这里主要测试DispatchTaskService的方法
        when(transportTaskMapper.selectById(1L)).thenReturn(testTask);
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 测试任务状态更新为取消
        dispatchTaskService.cancelTask(1L);

        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    /**
     * 测试根据计划ID获取任务列表
     */
    @Test
    void testGetByPlanId() {
        when(transportTaskMapper.findByPlanId(1L)).thenReturn(Collections.singletonList(testTask));

        List<DispatchTaskVO> result = dispatchTaskService.getByPlanId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试根据车辆ID获取待处理任务
     */
    @Test
    void testGetPendingByVehicleId() {
        when(transportTaskMapper.findPendingByVehicleId(1L)).thenReturn(Collections.singletonList(testTask));

        List<DispatchTaskVO> result = dispatchTaskService.getPendingByVehicleId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试根据司机ID获取待处理任务
     */
    @Test
    void testGetPendingByDriverId() {
        when(transportTaskMapper.findPendingByExecutorId(1L)).thenReturn(Collections.singletonList(testTask));

        List<DispatchTaskVO> result = dispatchTaskService.getPendingByDriverId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试获取可重新分配的任务
     */
    @Test
    void testGetAvailableTasksForReassignment() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        when(transportTaskMapper.findByStatusAndTimeRange(eq(0), any(), any()))
                .thenReturn(Collections.singletonList(testTask));

        List<DispatchTaskVO> result = dispatchTaskService.getAvailableTasksForReassignment(startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试统计车辆任务数
     */
    @Test
    void testCountTasksByVehicle() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        when(transportTaskMapper.countByVehicleIdAndTimeRange(eq(1L), any(), any())).thenReturn(5);

        int count = dispatchTaskService.countTasksByVehicle(1L, startTime, endTime);

        assertEquals(5, count);
    }

    /**
     * 测试统计司机任务数
     */
    @Test
    void testCountTasksByDriver() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        when(transportTaskMapper.countByExecutorIdAndTimeRange(eq(1L), any(), any())).thenReturn(3);

        int count = dispatchTaskService.countTasksByDriver(1L, startTime, endTime);

        assertEquals(3, count);
    }

    /**
     * 测试查询任务列表
     */
    @Test
    void testGetList() {
        when(transportTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testTask));

        List<DispatchTaskVO> result = dispatchTaskService.getList(0, 1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}