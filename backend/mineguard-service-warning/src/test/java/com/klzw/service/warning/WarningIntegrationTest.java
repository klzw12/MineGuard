package com.klzw.service.warning;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.dto.WarningHandleDTO;
import com.klzw.service.warning.dto.WarningRecordDTO;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningRecordStatusEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.mapper.WarningRecordMapper;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.service.WarningRecordService;
import com.klzw.service.warning.service.WarningRuleService;
import com.klzw.service.warning.service.WarningService;
import com.klzw.service.warning.vo.WarningRecordVO;
import com.klzw.service.warning.vo.WarningRuleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WarningService集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class WarningIntegrationTest {

    @Autowired
    private WarningRecordService warningRecordService;

    @Autowired
    private WarningRuleService warningRuleService;

    @Autowired
    private WarningService warningService;

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    @Autowired
    private WarningRuleMapper warningRuleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WarningRuleDTO testRuleDTO;
    private WarningRecordDTO testRecordDTO;
    private WarningTrackDTO testTrackDTO;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE warning_record");
        jdbcTemplate.execute("TRUNCATE TABLE warning_rule");
        
        // 初始化测试数据
        testRuleDTO = new WarningRuleDTO();
        testRuleDTO.setRuleName("集成测试预警规则");
        testRuleDTO.setRuleCode("INTEGRATION_TEST_001");
        testRuleDTO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRuleDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRuleDTO.setThresholdValue("{\"temperature\": 90}");
        testRuleDTO.setDescription("集成测试预警规则描述");

        testRecordDTO = new WarningRecordDTO();
        testRecordDTO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRecordDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRecordDTO.setVehicleId(1L);
        testRecordDTO.setDriverId(1L);
        testRecordDTO.setTripId(1L);
        testRecordDTO.setLongitude(116.404);
        testRecordDTO.setLatitude(39.915);
        testRecordDTO.setSpeed(new BigDecimal("60"));
        testRecordDTO.setWarningContent("集成测试预警内容");

        testTrackDTO = new WarningTrackDTO();
        testTrackDTO.setTripId(1L);
        testTrackDTO.setVehicleId(1L);
        testTrackDTO.setDriverId(1L);
        testTrackDTO.setLongitude(116.0);
        testTrackDTO.setLatitude(39.0);
        testTrackDTO.setSpeed(new BigDecimal("80"));
    }

    /**
     * 测试创建预警规则完整流程
     */
    @Test
    void testCreateWarningRuleFlow() {
        // 创建预警规则
        Long ruleId = warningRuleService.create(testRuleDTO);
        assertNotNull(ruleId);

        // 获取预警规则
        WarningRuleVO rule = warningRuleService.getById(ruleId);
        assertNotNull(rule);
        assertEquals("集成测试预警规则", rule.getRuleName());
        assertEquals("INTEGRATION_TEST_001", rule.getRuleCode());
        assertEquals("车辆故障", rule.getWarningTypeName());
        assertEquals("高风险", rule.getWarningLevelName());
    }

    /**
     * 测试更新预警规则
     */
    @Test
    void testUpdateWarningRuleFlow() {
        // 创建预警规则
        Long ruleId = warningRuleService.create(testRuleDTO);
        assertNotNull(ruleId);

        // 更新预警规则
        testRuleDTO.setRuleName("更新后的规则名称");
        testRuleDTO.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        warningRuleService.update(ruleId, testRuleDTO);

        // 验证更新
        WarningRuleVO rule = warningRuleService.getById(ruleId);
        assertEquals("更新后的规则名称", rule.getRuleName());
        assertEquals("中风险", rule.getWarningLevelName());
    }

    /**
     * 测试启用/禁用预警规则
     */
    @Test
    void testEnableDisableWarningRuleFlow() {
        // 创建预警规则
        Long ruleId = warningRuleService.create(testRuleDTO);

        // 禁用规则
        warningRuleService.disable(ruleId);
        WarningRuleVO disabledRule = warningRuleService.getById(ruleId);
        assertEquals(0, disabledRule.getStatus());

        // 启用规则
        warningRuleService.enable(ruleId);
        WarningRuleVO enabledRule = warningRuleService.getById(ruleId);
        assertEquals(1, enabledRule.getStatus());
    }

    /**
     * 测试创建预警记录完整流程
     */
    @Test
    void testCreateWarningRecordFlow() {
        // 创建预警记录
        Long recordId = warningRecordService.createWarning(testRecordDTO);
        assertNotNull(recordId);

        // 获取预警记录
        WarningRecordVO record = warningRecordService.getById(recordId);
        assertNotNull(record);
        assertNotNull(record.getWarningNo());
        assertEquals("集成测试预警内容", record.getWarningContent());
        assertEquals("车辆故障", record.getWarningTypeName());
        assertEquals("高风险", record.getWarningLevelName());
        assertEquals("待处理", record.getStatusName());
    }

    /**
     * 测试处理预警记录
     */
    @Test
    void testHandleWarningRecordFlow() {
        // 创建预警记录
        Long recordId = warningRecordService.createWarning(testRecordDTO);

        // 处理预警
        WarningHandleDTO handleDTO = new WarningHandleDTO();
        handleDTO.setHandleResult("已处理完成");
        handleDTO.setStatus(WarningRecordStatusEnum.RESOLVED.getCode());
        warningRecordService.handleWarning(recordId, handleDTO);

        // 验证处理结果
        WarningRecordVO record = warningRecordService.getById(recordId);
        assertEquals("已解决", record.getStatusName());
        assertEquals("已处理完成", record.getHandleResult());
        assertNotNull(record.getHandleTime());
    }

    /**
     * 测试忽略预警记录
     */
    @Test
    void testIgnoreWarningRecordFlow() {
        // 创建预警记录
        Long recordId = warningRecordService.createWarning(testRecordDTO);

        // 忽略预警
        warningRecordService.ignoreWarning(recordId);

        // 验证忽略结果
        WarningRecordVO record = warningRecordService.getById(recordId);
        assertEquals("已忽略", record.getStatusName());
        assertNotNull(record.getHandleTime());
    }

    /**
     * 测试根据行程ID查询预警记录
     */
    @Test
    void testGetWarningRecordsByTripId() {
        // 创建预警记录
        testRecordDTO.setTripId(100L);
        Long recordId = warningRecordService.createWarning(testRecordDTO);

        // 根据行程ID查询
        List<WarningRecordVO> records = warningRecordService.getByTripId(100L);

        assertNotNull(records);
        assertTrue(records.size() >= 1);
        assertEquals(100L, records.get(0).getTripId());
    }

    /**
     * 测试分页查询预警记录
     */
    @Test
    void testPageWarningRecords() {
        // 创建多个预警记录
        for (int i = 0; i < 5; i++) {
            testRecordDTO.setWarningContent("测试预警内容" + i);
            warningRecordService.createWarning(testRecordDTO);
        }

        // 分页查询
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(3);

        PageResult<WarningRecordVO> result = warningRecordService.page(pageRequest);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 5);
        assertEquals(3, result.getList().size());
    }

    /**
     * 测试获取预警统计
     */
    @Test
    void testGetWarningStatistics() {
        // 创建不同级别的预警记录
        testRecordDTO.setWarningLevel(WarningLevelEnum.LOW.getCode());
        warningRecordService.createWarning(testRecordDTO);

        testRecordDTO.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        warningRecordService.createWarning(testRecordDTO);

        testRecordDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        warningRecordService.createWarning(testRecordDTO);

        // 获取统计
        Map<String, Object> statistics = warningRecordService.getWarningStatistics(
                "2020-01-01 00:00:00", "2030-12-31 23:59:59");

        assertNotNull(statistics);
        assertTrue((Long) statistics.get("totalCount") >= 3);
        assertTrue((Long) statistics.get("lowLevelCount") >= 1);
        assertTrue((Long) statistics.get("mediumLevelCount") >= 1);
        assertTrue((Long) statistics.get("highLevelCount") >= 1);
    }

    /**
     * 测试获取预警趋势
     */
    @Test
    void testGetWarningTrend() {
        // 创建预警记录
        warningRecordService.createWarning(testRecordDTO);

        // 获取趋势
        List<Map<String, Object>> trend = warningRecordService.getWarningTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.size());
        trend.forEach(dayData -> {
            assertTrue(dayData.containsKey("date"));
            assertTrue(dayData.containsKey("count"));
        });
    }

    /**
     * 测试获取预警类型统计
     */
    @Test
    void testGetWarningTypeStatistics() {
        // 创建不同类型的预警记录
        testRecordDTO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        warningRecordService.createWarning(testRecordDTO);

        testRecordDTO.setWarningType(WarningTypeEnum.ROUTE_DEVIATION.getCode());
        warningRecordService.createWarning(testRecordDTO);

        // 获取类型统计
        Map<String, Object> statistics = warningRecordService.getWarningTypeStatistics(
                "2020-01-01 00:00:00", "2030-12-31 23:59:59");

        assertNotNull(statistics);
        assertTrue(statistics.containsKey("车辆故障"));
        assertTrue(statistics.containsKey("路线偏离"));
    }

    /**
     * 测试获取预警级别统计
     */
    @Test
    void testGetWarningLevelStatistics() {
        // 创建不同级别的预警记录
        testRecordDTO.setWarningLevel(WarningLevelEnum.LOW.getCode());
        warningRecordService.createWarning(testRecordDTO);

        testRecordDTO.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        warningRecordService.createWarning(testRecordDTO);

        testRecordDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        warningRecordService.createWarning(testRecordDTO);

        // 获取级别统计
        Map<String, Object> statistics = warningRecordService.getWarningLevelStatistics(
                "2020-01-01 00:00:00", "2030-12-31 23:59:59");

        assertNotNull(statistics);
        assertTrue(statistics.containsKey("低风险"));
        assertTrue(statistics.containsKey("中风险"));
        assertTrue(statistics.containsKey("高风险"));
    }

    /**
     * 测试删除预警规则
     */
    @Test
    void testDeleteWarningRule() {
        // 创建预警规则
        Long ruleId = warningRuleService.create(testRuleDTO);

        // 删除预警规则
        warningRuleService.delete(ruleId);

        // 验证删除（应该抛出异常）
        assertThrows(RuntimeException.class, () -> {
            warningRuleService.getById(ruleId);
        });
    }

    /**
     * 测试获取所有启用的预警规则
     */
    @Test
    void testListAllWarningRules() {
        // 创建多个预警规则
        warningRuleService.create(testRuleDTO);

        testRuleDTO.setRuleCode("INTEGRATION_TEST_002");
        testRuleDTO.setRuleName("集成测试预警规则2");
        warningRuleService.create(testRuleDTO);

        // 获取所有启用的规则
        List<WarningRuleVO> rules = warningRuleService.listAll();

        assertNotNull(rules);
        assertTrue(rules.size() >= 2);
    }

    /**
     * 测试根据编码获取预警规则
     */
    @Test
    void testGetWarningRuleByCode() {
        // 创建预警规则
        warningRuleService.create(testRuleDTO);

        // 根据编码查询
        WarningRuleVO rule = warningRuleService.getByCode("INTEGRATION_TEST_001");

        assertNotNull(rule);
        assertEquals("INTEGRATION_TEST_001", rule.getRuleCode());
        assertEquals("集成测试预警规则", rule.getRuleName());
    }

    /**
     * 测试分页查询预警规则
     */
    @Test
    void testPageWarningRules() {
        // 创建多个预警规则
        for (int i = 0; i < 5; i++) {
            testRuleDTO.setRuleCode("INTEGRATION_TEST_" + String.format("%03d", i));
            testRuleDTO.setRuleName("集成测试预警规则" + i);
            warningRuleService.create(testRuleDTO);
        }

        // 分页查询
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(3);

        PageResult<WarningRuleVO> result = warningRuleService.page(pageRequest);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 5);
        assertEquals(3, result.getList().size());
    }

    /**
     * 测试预警规则与预警记录关联
     */
    @Test
    void testWarningRuleAndRecordAssociation() {
        // 创建预警规则
        Long ruleId = warningRuleService.create(testRuleDTO);

        // 创建关联的预警记录
        testRecordDTO.setRuleId(ruleId);
        Long recordId = warningRecordService.createWarning(testRecordDTO);

        // 获取预警记录并验证关联
        WarningRecordVO record = warningRecordService.getById(recordId);
        assertNotNull(record);
        assertEquals(ruleId, record.getRuleId());
    }

    /**
     * 测试WarningService处理事件触发
     */
    @Test
    void testWarningServiceProcessEventTrigger() {
        // 处理事件触发
        WarningRecord result = warningService.processEventTrigger(testTrackDTO, "SPEED_ABNORMAL");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(1L, result.getTripId());
        assertEquals(1L, result.getVehicleId());
        assertEquals(1L, result.getDriverId());
    }

    /**
     * 测试WarningService处理预警轨迹
     */
    @Test
    void testWarningServiceProcessWarningTrack() {
        // 处理预警轨迹
        WarningRecord result = warningService.processWarningTrack(testTrackDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(1L, result.getTripId());
        assertEquals(1L, result.getVehicleId());
        assertEquals(1L, result.getDriverId());
    }

    /**
     * 测试WarningService保存预警记录
     */
    @Test
    void testWarningServiceSaveWarningRecord() {
        // 创建预警记录
        WarningRecord record = new WarningRecord();
        record.setWarningNo("TEST_WARN_001");
        record.setTripId(1L);
        record.setVehicleId(1L);
        record.setDriverId(1L);
        record.setWarningType(WarningTypeEnum.SPEED_ABNORMAL.getCode());
        record.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        record.setWarningContent("测试预警内容");

        // 保存预警记录
        boolean result = warningService.saveWarningRecord(record);

        // 验证结果
        assertTrue(result);
    }

    /**
     * 测试WarningService更新预警状态
     */
    @Test
    void testWarningServiceUpdateWarningStatus() {
        // 更新预警状态
        boolean result = warningService.updateWarningStatus(1L, 1);

        // 验证结果
        assertTrue(result);
    }

    /**
     * 测试WarningService推送预警通知
     */
    @Test
    void testWarningServicePushWarningNotification() {
        // 创建预警记录
        WarningRecord record = new WarningRecord();
        record.setId(1L);
        record.setWarningNo("TEST_WARN_001");
        record.setTripId(1L);
        record.setVehicleId(1L);
        record.setDriverId(1L);
        record.setWarningType(WarningTypeEnum.SPEED_ABNORMAL.getCode());
        record.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        record.setWarningContent("测试预警通知");

        // 推送预警通知
        warningService.pushWarningNotification(record);

        // 验证方法执行成功（无异常抛出）
        // 实际推送逻辑已在单元测试中验证
    }

    /**
     * 测试WarningService获取预警统计
     */
    @Test
    void testWarningServiceGetWarningStatistics() {
        // 获取预警统计
        Map<String, Object> statistics = warningService.getWarningStatistics("2024-01-01", "2024-01-31");

        // 验证结果
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalCount"));
        assertTrue(statistics.containsKey("lowLevelCount"));
        assertTrue(statistics.containsKey("mediumLevelCount"));
        assertTrue(statistics.containsKey("highLevelCount"));
        assertTrue(statistics.containsKey("pendingCount"));
        assertTrue(statistics.containsKey("handledCount"));
    }

    /**
     * 测试WarningService获取预警趋势
     */
    @Test
    void testWarningServiceGetWarningTrend() {
        // 获取预警趋势
        List<Map<String, Object>> trend = warningService.getWarningTrend(7);

        // 验证结果
        assertNotNull(trend);
        assertEquals(7, trend.size());
        for (Map<String, Object> dayData : trend) {
            assertNotNull(dayData.get("date"));
            assertNotNull(dayData.get("count"));
        }
    }
} 
