package com.klzw.service.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.result.Result;
import com.klzw.service.dispatch.constant.DispatchResultCode;
import com.klzw.service.dispatch.entity.InspectionTask;
import com.klzw.service.dispatch.entity.MaintenanceTask;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.exception.DispatchException;
import com.klzw.service.dispatch.mapper.InspectionTaskMapper;
import com.klzw.service.dispatch.mapper.MaintenanceTaskMapper;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.service.impl.DispatchServiceImpl;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DispatchServiceImplTest {

    @InjectMocks
    private DispatchServiceImpl dispatchService;

    @Mock
    private TransportTaskMapper transportTaskMapper;

    @Mock
    private RouteTemplateMapper routeTemplateMapper;

    @Mock
    private MaintenanceTaskMapper maintenanceTaskMapper;

    @Mock
    private InspectionTaskMapper inspectionTaskMapper;

    @Mock
    private DriverClient driverClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private UserClient userClient;

    @Mock
    private MessageClient messageClient;

    @Mock
    private TripClient tripClient;

    private TransportTask transportTask;
    private DriverInfo driverInfo;
    private VehicleInfo vehicleInfo;

    @BeforeEach
    void setUp() {
        transportTask = new TransportTask();
        transportTask.setId(1L);
        transportTask.setTaskNo("TASK202301010001");
        transportTask.setStatus(0);
        transportTask.setPriority("normal");
        transportTask.setCargoWeight(new BigDecimal(10));
        transportTask.setScheduledStartTime(LocalDateTime.now());
        transportTask.setScheduledEndTime(LocalDateTime.now().plusHours(2));

        driverInfo = new DriverInfo();
        driverInfo.setId(1L);
        driverInfo.setUserId(100L);
        driverInfo.setScore(90);

        vehicleInfo = new VehicleInfo();
        vehicleInfo.setId(1L);
        vehicleInfo.setVehicleNo("京A12345");
        vehicleInfo.setStatus(0);
    }

    @Test
    void executeDispatch() {
        // 模拟数据
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        
        // 模拟获取可用司机
        List<DriverInfo> drivers = new ArrayList<>();
        drivers.add(driverInfo);
        Result<List<DriverInfo>> driversResult = Result.success(new ArrayList<>());
        driversResult.setData(drivers);
        when(driverClient.getAvailableDrivers()).thenReturn(driversResult);
        
        // 模拟已分配司机列表
        when(transportTaskMapper.findAssignedButNotAcceptedDriverIds()).thenReturn(new ArrayList<>());
        
        // 模拟获取司机常用车辆
        List<DriverVehicleInfo> commonVehicles = new ArrayList<>();
        DriverVehicleInfo commonVehicle = new DriverVehicleInfo();
        commonVehicle.setVehicleId(1L);
        commonVehicles.add(commonVehicle);
        Result<List<DriverVehicleInfo>> commonVehiclesResult = Result.success(new ArrayList<>());
        commonVehiclesResult.setData(commonVehicles);
        when(driverClient.getCommonVehicles(1L)).thenReturn(commonVehiclesResult);
        
        // 模拟车辆可用性检查
        Result<VehicleInfo> vehicleResult = Result.success(null);
        vehicleResult.setData(vehicleInfo);
        when(vehicleClient.getById(1L)).thenReturn(vehicleResult);
        when(transportTaskMapper.findAssignedButNotAcceptedVehicleIds()).thenReturn(new ArrayList<>());
        
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);
        
        // 模拟创建行程
        Result<Long> tripResult = Result.success(1L);
        tripResult.setCode(200);
        tripResult.setData(1L);
        when(tripClient.createTrip(any(TripCreateRequest.class))).thenReturn(Mono.just(tripResult));

        // 执行调度
        boolean result = dispatchService.executeDispatch(1L);
        assertTrue(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(driverClient, times(1)).getAvailableDrivers();
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
        verify(tripClient, times(1)).createTrip(any(TripCreateRequest.class));
    }

    @Test
    void executeDispatch_NoDriver() {
        // 模拟数据
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        
        // 模拟没有可用司机
        Result<List<DriverInfo>> driversResult = Result.success(new ArrayList<>());
        driversResult.setData(new ArrayList<>());
        when(driverClient.getAvailableDrivers()).thenReturn(driversResult);

        // 执行调度，预期抛出异常
        DispatchException exception = assertThrows(DispatchException.class, () -> dispatchService.executeDispatch(1L));
        assertEquals(DispatchResultCode.NO_AVAILABLE_DRIVER.getCode(), exception.getCode());
    }

    @Test
    void executeDispatch_NoVehicle() {
        // 模拟数据
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        
        // 模拟获取可用司机
        List<DriverInfo> drivers = new ArrayList<>();
        drivers.add(driverInfo);
        Result<List<DriverInfo>> driversResult = Result.success(new ArrayList<>());
        driversResult.setData(drivers);
        when(driverClient.getAvailableDrivers()).thenReturn(driversResult);
        
        // 模拟已分配司机列表
        when(transportTaskMapper.findAssignedButNotAcceptedDriverIds()).thenReturn(new ArrayList<>());
        
        // 模拟没有常用车辆
        Result<List<DriverVehicleInfo>> commonVehiclesResult = Result.success(new ArrayList<>());
        commonVehiclesResult.setData(new ArrayList<>());
        when(driverClient.getCommonVehicles(1L)).thenReturn(commonVehiclesResult);
        
        // 模拟没有最佳车辆
        Result<List<VehicleInfo>> vehiclesResult = Result.success(new ArrayList<>());
        vehiclesResult.setData(new ArrayList<>());
        when(vehicleClient.selectBestVehicle(anyLong(), any(), any(), any(), any())).thenReturn(vehiclesResult);

        // 执行调度，预期抛出异常
        DispatchException exception = assertThrows(DispatchException.class, () -> dispatchService.executeDispatch(1L));
        assertEquals(DispatchResultCode.NO_AVAILABLE_VEHICLE.getCode(), exception.getCode());
    }

    @Test
    void dynamicAdjustForVehicleFault() {
        // 模拟数据
        List<TransportTask> pendingTasks = new ArrayList<>();
        pendingTasks.add(transportTask);
        when(transportTaskMapper.findPendingByVehicleId(1L)).thenReturn(pendingTasks);
        
        // 模拟获取最佳车辆
        List<VehicleInfo> vehicles = new ArrayList<>();
        vehicles.add(vehicleInfo);
        Result<List<VehicleInfo>> vehiclesResult = Result.success(new ArrayList<>());
        vehiclesResult.setData(vehicles);
        when(vehicleClient.selectBestVehicle(anyLong(), any(), any(), any(), any())).thenReturn(vehiclesResult);
        
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);
        
        // 模拟发送通知
        doNothing().when(messageClient).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());

        // 执行动态调整
        dispatchService.dynamicAdjustForVehicleFault(1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).findPendingByVehicleId(1L);
        verify(vehicleClient, times(1)).selectBestVehicle(anyLong(), any(), any(), any(), any());
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void dynamicAdjustForDriverLeave() {
        // 模拟数据
        List<TransportTask> pendingTasks = new ArrayList<>();
        pendingTasks.add(transportTask);
        when(transportTaskMapper.findPendingByExecutorId(1L)).thenReturn(pendingTasks);
        
        // 模拟获取可用司机
        List<DriverInfo> drivers = new ArrayList<>();
        drivers.add(driverInfo);
        Result<List<DriverInfo>> driversResult = Result.success(new ArrayList<>());
        driversResult.setData(drivers);
        when(driverClient.getAvailableDrivers()).thenReturn(driversResult);
        
        // 模拟已分配司机列表
        when(transportTaskMapper.findAssignedButNotAcceptedDriverIds()).thenReturn(new ArrayList<>());
        
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);
        
        // 模拟发送通知
        doNothing().when(messageClient).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());

        // 执行动态调整
        dispatchService.dynamicAdjustForDriverLeave(1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).findPendingByExecutorId(1L);
        verify(driverClient, times(1)).getAvailableDrivers();
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void dynamicAdjustForRouteBlock() {
        // 模拟数据
        List<TransportTask> pendingTasks = new ArrayList<>();
        pendingTasks.add(transportTask);
        when(transportTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(pendingTasks);
        
        // 模拟查找替代路线
        RouteTemplate routeTemplate = new RouteTemplate();
        routeTemplate.setId(2L);
        routeTemplate.setStartLocation("起点");
        routeTemplate.setEndLocation("终点");
        List<RouteTemplate> routes = new ArrayList<>();
        routes.add(routeTemplate);
        when(routeTemplateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(routes);
        
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 执行动态调整
        dispatchService.dynamicAdjustForRouteBlock(1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(routeTemplateMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void createDispatchTask() {
        // 模拟插入任务
        when(transportTaskMapper.insert(any(TransportTask.class))).thenAnswer(invocation -> {
            TransportTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        // 创建任务
        TransportTask result = dispatchService.createDispatchTask(transportTask);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(0, result.getStatus());
        assertNotNull(result.getTaskNo());

        // 验证方法调用
        verify(transportTaskMapper, times(1)).insert(any(TransportTask.class));
    }

    @Test
    void updateDispatchTask() {
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 更新任务
        TransportTask result = dispatchService.updateDispatchTask(transportTask);
        assertNotNull(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void deleteDispatchTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟删除任务
        when(transportTaskMapper.deleteById(1L)).thenReturn(1);

        // 删除任务
        boolean result = dispatchService.deleteDispatchTask(1L);
        assertTrue(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).deleteById(1L);
    }

    @Test
    void deleteDispatchTask_NotFound() {
        // 模拟任务不存在
        when(transportTaskMapper.selectById(1L)).thenReturn(null);

        // 删除任务
        boolean result = dispatchService.deleteDispatchTask(1L);
        assertFalse(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, never()).updateById(any(TransportTask.class));
    }

    @Test
    void getDispatchTasksByPlanId() {
        // 模拟查询任务
        List<TransportTask> tasks = new ArrayList<>();
        tasks.add(transportTask);
        when(transportTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(tasks);

        // 获取任务
        List<TransportTask> result = dispatchService.getDispatchTasksByPlanId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getPendingTasksByVehicle() {
        // 模拟查询任务
        List<TransportTask> tasks = new ArrayList<>();
        tasks.add(transportTask);
        when(transportTaskMapper.findPendingByVehicleId(1L)).thenReturn(tasks);

        // 获取任务
        List<TransportTask> result = dispatchService.getPendingTasksByVehicle(1L);
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(transportTaskMapper, times(1)).findPendingByVehicleId(1L);
    }

    @Test
    void getPendingTasksByDriver() {
        // 模拟查询任务
        List<TransportTask> tasks = new ArrayList<>();
        tasks.add(transportTask);
        when(transportTaskMapper.findPendingByExecutorId(100L)).thenReturn(tasks);
        


        // 获取任务
        List<DispatchTaskVO> result = dispatchService.getPendingTasksByDriver(100L);
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(transportTaskMapper, times(1)).findPendingByExecutorId(100L);
    }

    @Test
    void assignVehicle() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 分配车辆
        dispatchService.assignVehicle(1L, 1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void assignDriver() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 分配司机
        dispatchService.assignDriver(1L, 1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void startTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);
        
        // 模拟创建行程
        Result<Long> tripResult = Result.success(1L);
        tripResult.setCode(200);
        tripResult.setData(1L);
        when(tripClient.createTrip(any(TripCreateRequest.class))).thenReturn(Mono.just(tripResult));

        // 开始任务
        Long tripId = dispatchService.startTask(1L);
        assertNotNull(tripId);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(2)).updateById(any(TransportTask.class)); // 一次更新状态，一次更新tripId
        verify(tripClient, times(1)).createTrip(any(TripCreateRequest.class));
    }

    @Test
    void completeTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 完成任务
        dispatchService.completeTask(1L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void cancelDispatchTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);

        // 取消任务
        boolean result = dispatchService.cancelDispatchTask(1L);
        assertTrue(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
    }

    @Test
    void cancelDispatchTask_NotFound() {
        // 模拟任务不存在
        when(transportTaskMapper.selectById(1L)).thenReturn(null);

        // 取消任务
        boolean result = dispatchService.cancelDispatchTask(1L);
        assertFalse(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, never()).updateById(any(TransportTask.class));
    }

    @Test
    void getDispatchTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);

        // 获取任务
        TransportTask result = dispatchService.getDispatchTask(1L);
        assertNotNull(result);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
    }

    @Test
    void getDispatchTaskList() {
        // 模拟查询任务
        List<TransportTask> tasks = new ArrayList<>();
        tasks.add(transportTask);
        when(transportTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(tasks);

        // 获取任务列表
        List<TransportTask> result = dispatchService.getDispatchTaskList(0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void reassignTask() {
        // 模拟查询任务
        when(transportTaskMapper.selectById(1L)).thenReturn(transportTask);
        // 模拟更新任务
        when(transportTaskMapper.updateById(any(TransportTask.class))).thenReturn(1);
        // 模拟发送通知
        doNothing().when(messageClient).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());

        // 重新分配任务
        dispatchService.reassignTask(1L, 2L, 2L);

        // 验证方法调用
        verify(transportTaskMapper, times(1)).selectById(1L);
        verify(transportTaskMapper, times(1)).updateById(any(TransportTask.class));
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }
}
