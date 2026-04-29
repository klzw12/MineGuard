package com.klzw.service.dispatch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.domain.dto.DriverInfo;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.service.dispatch.dto.DispatchPlanDTO;
import com.klzw.service.dispatch.entity.DispatchPlan;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.mapper.DispatchPlanMapper;
import com.klzw.service.dispatch.mapper.InspectionTaskMapper;
import com.klzw.service.dispatch.mapper.MaintenanceTaskMapper;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.mapper.TransportTaskMapper;
import com.klzw.service.dispatch.service.impl.DispatchPlanServiceImpl;
import com.klzw.service.dispatch.vo.DispatchPlanVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DispatchPlanService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class DispatchPlanServiceTest {

    @InjectMocks
    private DispatchPlanServiceImpl dispatchPlanService;

    @Mock
    private DispatchPlanMapper dispatchPlanMapper;

    @Mock
    private RouteTemplateMapper routeTemplateMapper;

    @Mock
    private DriverClient driverClient;

    @Mock
    private UserClient userClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private TransportTaskMapper transportTaskMapper;

    @Mock
    private MaintenanceTaskMapper maintenanceTaskMapper;

    @Mock
    private InspectionTaskMapper inspectionTaskMapper;

    @Mock
    private IMessageProducer messageProducer;

    private DispatchPlan testPlan;
    private DispatchPlanDTO testPlanDTO;
    private RouteTemplate testRoute;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testPlan = new DispatchPlan();
        testPlan.setId(1L);
        testPlan.setPlanNo("DP202604090001");
        testPlan.setPlanName("测试调度计划");
        testPlan.setPlanDate(LocalDate.now());
        testPlan.setPlanType(1);
        testPlan.setStatus(0);
        testPlan.setCompletedTrips(0);
        testPlan.setPlannedTrips(1);
        testPlan.setPlannedCargoWeight(new BigDecimal("100"));
        testPlan.setStartLocation("北京");
        testPlan.setEndLocation("上海");
        testPlan.setCreateTime(LocalDateTime.now());
        testPlan.setUpdateTime(LocalDateTime.now());

        testPlanDTO = new DispatchPlanDTO();
        testPlanDTO.setPlanName("测试调度计划");
        testPlanDTO.setPlanDate(LocalDate.now());
        testPlanDTO.setPlanType(1);
        testPlanDTO.setPlannedTrips(1);
        testPlanDTO.setPlannedCargoWeight(new BigDecimal("100"));

        testRoute = new RouteTemplate();
        testRoute.setId(1L);
        testRoute.setRouteName("北京-上海线路");
        testRoute.setStartLocation("北京");
        testRoute.setEndLocation("上海");
        testRoute.setStartLongitude(116.404);
        testRoute.setStartLatitude(39.915);
        testRoute.setEndLongitude(121.473);
        testRoute.setEndLatitude(31.230);
    }

    @AfterEach
    void tearDown() {
        reset(dispatchPlanMapper, routeTemplateMapper, driverClient, userClient,
                vehicleClient, transportTaskMapper, maintenanceTaskMapper,
                inspectionTaskMapper, messageProducer);
    }

    /**
     * 测试创建调度计划
     */
    @Test
    void testCreateDispatchPlan() {
        // 准备测试数据
        testPlanDTO.setRouteId(1L);

        // Mock路由模板查询
        when(routeTemplateMapper.selectById(1L)).thenReturn(testRoute);
        // Mock数据库插入
        when(dispatchPlanMapper.insert(any(DispatchPlan.class))).thenAnswer(invocation -> {
            DispatchPlan plan = invocation.getArgument(0);
            plan.setId(1L);
            return 1;
        });

        // 执行测试
        Long result = dispatchPlanService.create(testPlanDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
        verify(dispatchPlanMapper, times(1)).insert(any(DispatchPlan.class));
    }

    /**
     * 测试创建调度计划时不指定路线
     */
    @Test
    void testCreateDispatchPlanWithoutRoute() {
        // 不设置路线ID
        when(dispatchPlanMapper.insert(any(DispatchPlan.class))).thenAnswer(invocation -> {
            DispatchPlan plan = invocation.getArgument(0);
            plan.setId(2L);
            return 1;
        });

        Long result = dispatchPlanService.create(testPlanDTO);

        assertNotNull(result);
        assertEquals(2L, result);
        verify(dispatchPlanMapper, times(1)).insert(any(DispatchPlan.class));
    }

    /**
     * 测试更新调度计划
     */
    @Test
    void testUpdateDispatchPlan() {
        // Mock查询
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        // 执行更新
        testPlanDTO.setPlanName("更新后的计划名称");
        dispatchPlanService.update(1L, testPlanDTO);

        // 验证更新调用
        verify(dispatchPlanMapper, times(1)).updateById(any(DispatchPlan.class));
    }

    /**
     * 测试更新不存在的调度计划
     */
    @Test
    void testUpdateDispatchPlanNotFound() {
        when(dispatchPlanMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.update(999L, testPlanDTO);
        });
    }

    /**
     * 测试更新非待执行状态的调度计划
     */
    @Test
    void testUpdateDispatchPlanInvalidStatus() {
        testPlan.setStatus(1); // 执行中状态
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.update(1L, testPlanDTO);
        });
    }

    /**
     * 测试获取调度计划
     */
    @Test
    void testGetDispatchPlanById() {
        // Mock查询
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        // Mock车辆查询
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 1L);
        vehicleMap.put("vehicleNo", "京A12345");
        Result<Map<String, Object>> vehicleResult = Result.success(vehicleMap);
        when(vehicleClient.getById(1L)).thenReturn(vehicleResult);

        // Mock司机查询
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(1L);
        driverInfo.setUserId(1L);
        driverInfo.setDriverName("张三");
        Result<DriverInfo> driverResult = Result.success(driverInfo);
        when(driverClient.getById(1L)).thenReturn(driverResult);

        // Mock路线查询
        when(routeTemplateMapper.selectById(1L)).thenReturn(testRoute);

        // 执行测试
        DispatchPlanVO result = dispatchPlanService.getById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("测试调度计划", result.getPlanName());
    }

    /**
     * 测试获取不存在的调度计划
     */
    @Test
    void testGetDispatchPlanByIdNotFound() {
        when(dispatchPlanMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.getById(999L);
        });
    }

    /**
     * 测试分页查询调度计划
     */
    @Test
    void testPageDispatchPlans() {
        // 准备分页数据
        Page<DispatchPlan> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(testPlan));
        page.setTotal(1);

        when(dispatchPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<DispatchPlanVO> result = dispatchPlanService.page(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    /**
     * 测试分页查询为空的情况
     */
    @Test
    void testPageDispatchPlansEmpty() {
        Page<DispatchPlan> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(dispatchPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<DispatchPlanVO> result = dispatchPlanService.page(pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    /**
     * 测试删除调度计划
     */
    @Test
    void testDeleteDispatchPlan() {
        testPlan.setStatus(0);
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);
        when(dispatchPlanMapper.deleteById(1L)).thenReturn(1);

        dispatchPlanService.delete(1L);

        verify(dispatchPlanMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试删除执行中的调度计划
     */
    @Test
    void testDeleteDispatchPlanExecuting() {
        testPlan.setStatus(1);
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.delete(1L);
        });
    }

    /**
     * 测试执行调度计划
     */
    @Test
    void testExecuteDispatchPlan() {
        testPlan.setStatus(0);
        testPlan.setPlanType(1);

        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);
        when(dispatchPlanMapper.updateById(any(DispatchPlan.class))).thenReturn(1);

        // Mock获取可用司机
        DriverInfo driver = new DriverInfo();
        driver.setId(1L);
        driver.setUserId(1L);
        driver.setDriverName("张三");
        driver.setScore(100);

        Result<List<DriverInfo>> driverResult = Result.success(Collections.singletonList(driver));
        when(driverClient.getAvailableDrivers()).thenReturn(driverResult);

        // Mock获取可用车辆
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 1L);
        vehicleMap.put("vehicleNo", "京A12345");
        vehicleMap.put("status", 0);

        Result<List<Map<String, Object>>> vehicleResult = Result.success(Collections.singletonList(vehicleMap));
        when(vehicleClient.getAvailableVehicles()).thenReturn(vehicleResult);

        // Mock分配但未接单的司机/车辆查询
        when(transportTaskMapper.findAssignedButNotAcceptedDriverIds()).thenReturn(Collections.emptyList());
        when(transportTaskMapper.findAssignedButNotAcceptedVehicleIds()).thenReturn(Collections.emptyList());

        // 执行测试
        dispatchPlanService.execute(1L);

        // 验证计划状态更新为执行中
        verify(dispatchPlanMapper, atLeastOnce()).updateById(any(DispatchPlan.class));
    }

    /**
     * 测试执行不存在的调度计划
     */
    @Test
    void testExecuteDispatchPlanNotFound() {
        when(dispatchPlanMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.execute(999L);
        });
    }

    /**
     * 测试执行非待执行状态的调度计划
     */
    @Test
    void testExecuteDispatchPlanInvalidStatus() {
        testPlan.setStatus(2); // 已完成状态
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.execute(1L);
        });
    }

    /**
     * 测试完成调度计划
     */
    @Test
    void testCompleteDispatchPlan() {
        testPlan.setStatus(1);
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);
        when(dispatchPlanMapper.updateById(any(DispatchPlan.class))).thenReturn(1);

        dispatchPlanService.complete(1L);

        verify(dispatchPlanMapper, times(1)).updateById(any(DispatchPlan.class));
    }

    /**
     * 测试完成非执行中的调度计划
     */
    @Test
    void testCompleteDispatchPlanInvalidStatus() {
        testPlan.setStatus(0); // 待执行状态
        when(dispatchPlanMapper.selectById(1L)).thenReturn(testPlan);

        assertThrows(RuntimeException.class, () -> {
            dispatchPlanService.complete(1L);
        });
    }

    /**
     * 测试按日期查询调度计划
     */
    @Test
    void testGetByDate() {
        when(dispatchPlanMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testPlan));

        List<DispatchPlanVO> result = dispatchPlanService.getByDate(LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试按状态查询调度计划列表
     */
    @Test
    void testListByStatus() {
        when(dispatchPlanMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testPlan));

        List<DispatchPlanVO> result = dispatchPlanService.list(0, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}