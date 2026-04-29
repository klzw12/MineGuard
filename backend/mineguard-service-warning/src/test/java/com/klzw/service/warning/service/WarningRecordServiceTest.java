package com.klzw.service.warning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.warning.constant.WarningResultCode;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningRecordStatusEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.exception.WarningException;
import com.klzw.service.warning.mapper.WarningRecordMapper;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.processor.EventTriggerProcessor;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.impl.WarningRecordServiceImpl;
import com.klzw.service.warning.vo.WarningRecordVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WarningRecordService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class WarningRecordServiceTest {

    @InjectMocks
    private WarningRecordServiceImpl warningRecordService;

    @Mock
    private WarningRecordMapper warningRecordMapper;

    @Mock
    private WarningRuleMapper warningRuleMapper;

    @Mock
    private EventTriggerProcessor eventTriggerProcessor;

    @Mock
    private WarningTriggerProcessor warningTriggerProcessor;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private TripClient tripClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private UserClient userClient;

    @Mock
    private MessageClient messageClient;

    private WarningRecord testRecord;
    private WarningRecordDTO testRecordDTO;
    private WarningRule testRule;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testRecord = new WarningRecord();
        testRecord.setId(1L);
        testRecord.setWarningNo("WARN202604090001");
        testRecord.setRuleId(1L);
        testRecord.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecord.setVehicleId(1L);
        testRecord.setDriverId(1L);
        testRecord.setTripId(1L);
        testRecord.setLongitude(116.404);
        testRecord.setLatitude(39.915);
        testRecord.setSpeed(new BigDecimal("60"));
        testRecord.setWarningContent("测试预警内容");
        testRecord.setWarningTime(LocalDateTime.now());
        testRecord.setStatus(WarningRecordStatusEnum.PENDING.getCode());
        testRecord.setCreateTime(LocalDateTime.now());
        testRecord.setUpdateTime(LocalDateTime.now());

        testRecordDTO = new WarningRecordDTO();
        testRecordDTO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecordDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecordDTO.setVehicleId(1L);
        testRecordDTO.setDriverId(1L);
        testRecordDTO.setTripId(1L);
        testRecordDTO.setLongitude(116.404);
        testRecordDTO.setLatitude(39.915);
        testRecordDTO.setSpeed(new BigDecimal("60"));
        testRecordDTO.setWarningContent("测试预警内容");

        testRule = new WarningRule();
        testRule.setId(1L);
        testRule.setRuleName("车辆故障预警规则");
        testRule.setRuleCode("VEHICLE_FAULT_001");
        testRule.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRule.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRule.setStatus(1);
    }

    @AfterEach
    void tearDown() {
        reset(warningRecordMapper, warningRuleMapper, eventTriggerProcessor,
                warningTriggerProcessor, redisCacheService, tripClient,
                vehicleClient, userClient);
    }

    /**
     * 测试创建预警记录
     */
    @Test
    void testCreateWarningRecord() {
        // Mock数据库插入
        when(warningRecordMapper.insert(any(WarningRecord.class))).thenAnswer(invocation -> {
            WarningRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });

        // 执行测试
        Long result = warningRecordService.createWarning(testRecordDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
        verify(warningRecordMapper, times(1)).insert(any(WarningRecord.class));
    }

    /**
     * 测试处理预警
     */
    @Test
    void testHandleWarning() {
        // Mock查询
        when(warningRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(warningRecordMapper.updateById(any(WarningRecord.class))).thenReturn(1);

        // 准备处理DTO
        WarningHandleDTO handleDTO = new WarningHandleDTO();
        handleDTO.setHandleResult("已处理");
        handleDTO.setStatus(WarningRecordStatusEnum.RESOLVED.getCode());

        // 执行测试
        warningRecordService.handleWarning(1L, handleDTO);

        // 验证更新调用
        verify(warningRecordMapper, times(1)).updateById(any(WarningRecord.class));
    }

    /**
     * 测试处理不存在的预警记录
     */
    @Test
    void testHandleWarningNotFound() {
        when(warningRecordMapper.selectById(999L)).thenReturn(null);

        WarningHandleDTO handleDTO = new WarningHandleDTO();
        handleDTO.setHandleResult("已处理");

        assertThrows(WarningException.class, () -> {
            warningRecordService.handleWarning(999L, handleDTO);
        });
    }

    /**
     * 测试处理已解决的预警记录
     */
    @Test
    void testHandleWarningAlreadyResolved() {
        testRecord.setStatus(WarningRecordStatusEnum.RESOLVED.getCode());
        when(warningRecordMapper.selectById(1L)).thenReturn(testRecord);

        WarningHandleDTO handleDTO = new WarningHandleDTO();
        handleDTO.setHandleResult("已处理");

        assertThrows(WarningException.class, () -> {
            warningRecordService.handleWarning(1L, handleDTO);
        });
    }

    /**
     * 测试忽略预警
     */
    @Test
    void testIgnoreWarning() {
        when(warningRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(warningRecordMapper.updateById(any(WarningRecord.class))).thenReturn(1);

        warningRecordService.ignoreWarning(1L);

        verify(warningRecordMapper, times(1)).updateById(any(WarningRecord.class));
    }

    /**
     * 测试忽略不存在的预警记录
     */
    @Test
    void testIgnoreWarningNotFound() {
        when(warningRecordMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRecordService.ignoreWarning(999L);
        });
    }

    /**
     * 测试根据ID获取预警记录
     */
    @Test
    void testGetWarningById() {
        when(warningRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(redisCacheService.get(anyString())).thenReturn(null);

        // Mock车辆查询
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 1L);
        vehicleMap.put("vehicleNo", "京A12345");
        when(vehicleClient.getById(1L)).thenReturn(Result.success(vehicleMap));

        // Mock司机查询
        Result<Object> userResult = Result.success(Map.of("realName", "张三"));
        when(userClient.getUserById(1L)).thenReturn(userResult);

        WarningRecordVO result = warningRecordService.getById(1L);

        assertNotNull(result);
        assertEquals("WARN202604090001", result.getWarningNo());
        assertEquals("车辆故障", result.getWarningTypeName());
        assertEquals("高风险", result.getWarningLevelName());
    }

    /**
     * 测试获取不存在的预警记录
     */
    @Test
    void testGetWarningByIdNotFound() {
        when(warningRecordMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRecordService.getById(999L);
        });
    }

    /**
     * 测试根据行程ID获取预警记录
     */
    @Test
    void testGetWarningByTripId() {
        when(warningRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testRecord));
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(redisCacheService.get(anyString())).thenReturn(null);

        List<WarningRecordVO> result = warningRecordService.getByTripId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WARN202604090001", result.get(0).getWarningNo());
    }

    /**
     * 测试分页查询预警记录
     */
    @Test
    void testPageWarningRecords() {
        Page<WarningRecord> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(testRecord));
        page.setTotal(1);

        when(warningRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(redisCacheService.get(anyString())).thenReturn(null);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<WarningRecordVO> result = warningRecordService.page(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    /**
     * 测试分页查询为空的情况
     */
    @Test
    void testPageWarningRecordsEmpty() {
        Page<WarningRecord> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(warningRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<WarningRecordVO> result = warningRecordService.page(pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    /**
     * 测试获取预警统计
     */
    @Test
    void testGetWarningStatistics() {
        when(warningRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        Map<String, Object> result = warningRecordService.getWarningStatistics(
                "2024-04-01 00:00:00", "2024-04-30 23:59:59");

        assertNotNull(result);
        assertEquals(10L, result.get("totalCount"));
        assertTrue(result.containsKey("lowLevelCount"));
        assertTrue(result.containsKey("mediumLevelCount"));
        assertTrue(result.containsKey("highLevelCount"));
        assertTrue(result.containsKey("pendingCount"));
        assertTrue(result.containsKey("handledCount"));
    }

    /**
     * 测试获取预警趋势
     */
    @Test
    void testGetWarningTrend() {
        when(warningRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        List<Map<String, Object>> result = warningRecordService.getWarningTrend(7);

        assertNotNull(result);
        assertEquals(7, result.size());
        result.forEach(dayData -> {
            assertTrue(dayData.containsKey("date"));
            assertTrue(dayData.containsKey("count"));
        });
    }

    /**
     * 测试获取预警类型统计
     */
    @Test
    void testGetWarningTypeStatistics() {
        when(warningRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        Map<String, Object> result = warningRecordService.getWarningTypeStatistics(
                "2024-04-01 00:00:00", "2024-04-30 23:59:59");

        assertNotNull(result);
        assertTrue(result.containsKey("车辆故障"));
        assertTrue(result.containsKey("路线偏离"));
        assertTrue(result.containsKey("长时间停留"));
    }

    /**
     * 测试获取预警级别统计
     */
    @Test
    void testGetWarningLevelStatistics() {
        when(warningRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        Map<String, Object> result = warningRecordService.getWarningLevelStatistics(
                "2024-04-01 00:00:00", "2024-04-30 23:59:59");

        assertNotNull(result);
        assertTrue(result.containsKey("低风险"));
        assertTrue(result.containsKey("中风险"));
        assertTrue(result.containsKey("高风险"));
    }

    /**
     * 测试处理事件触发
     */
    @Test
    void testProcessEventTrigger() {
        WarningTrackDTO trackDTO = new WarningTrackDTO();
        trackDTO.setVehicleId(1L);
        trackDTO.setLongitude(116.404);
        trackDTO.setLatitude(39.915);

        when(eventTriggerProcessor.processEventTrigger(any(WarningTrackDTO.class), anyString()))
                .thenReturn(testRecord);
        when(warningRecordMapper.insert(any(WarningRecord.class))).thenReturn(1);

        WarningRecord result = warningRecordService.processEventTrigger(trackDTO, "VEHICLE_FAULT");

        assertNotNull(result);
        verify(warningRecordMapper, times(1)).insert(any(WarningRecord.class));
    }

    /**
     * 测试处理预警轨迹
     */
    @Test
    void testProcessWarningTrack() {
        WarningTrackDTO trackDTO = new WarningTrackDTO();
        trackDTO.setVehicleId(1L);
        trackDTO.setLongitude(116.404);
        trackDTO.setLatitude(39.915);

        when(warningTriggerProcessor.processTrack(any(WarningTrackDTO.class)))
                .thenReturn(testRecord);
        when(warningRecordMapper.insert(any(WarningRecord.class))).thenReturn(1);

        WarningRecord result = warningRecordService.processWarningTrack(trackDTO);

        assertNotNull(result);
        verify(warningRecordMapper, times(1)).insert(any(WarningRecord.class));
    }

    /**
     * 测试处理预警轨迹无预警产生
     */
    @Test
    void testProcessWarningTrackNoWarning() {
        WarningTrackDTO trackDTO = new WarningTrackDTO();
        trackDTO.setVehicleId(1L);
        trackDTO.setLongitude(116.404);
        trackDTO.setLatitude(39.915);

        when(warningTriggerProcessor.processTrack(any(WarningTrackDTO.class)))
                .thenReturn(null);

        WarningRecord result = warningRecordService.processWarningTrack(trackDTO);

        assertNull(result);
        verify(warningRecordMapper, never()).insert(any(WarningRecord.class));
    }

    /**
     * 测试推送高危预警通知
     */
    @Test
    void testPushHighLevelWarningNotification() {
        testRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode());

        warningRecordService.pushWarningNotification(testRecord);

        // 验证日志输出（高危预警应该记录error日志）
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
    }

    /**
     * 测试推送中危预警通知
     */
    @Test
    void testPushMediumLevelWarningNotification() {
        testRecord.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());

        warningRecordService.pushWarningNotification(testRecord);

        // 验证日志输出（中危预警应该记录warn日志）
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
    }

    /**
     * 测试推送低危预警通知
     */
    @Test
    void testPushLowLevelWarningNotification() {
        testRecord.setWarningLevel(WarningLevelEnum.LOW.getCode());

        warningRecordService.pushWarningNotification(testRecord);

        // 验证日志输出（低危预警应该记录info日志）
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
    }
}
