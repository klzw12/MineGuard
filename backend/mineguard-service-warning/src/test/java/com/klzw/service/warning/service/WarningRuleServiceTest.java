package com.klzw.service.warning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.exception.WarningException;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.service.impl.WarningRuleServiceImpl;
import com.klzw.service.warning.vo.WarningRuleVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WarningRuleService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class WarningRuleServiceTest {

    @InjectMocks
    private WarningRuleServiceImpl warningRuleService;

    @Mock
    private WarningRuleMapper warningRuleMapper;

    private WarningRule testRule;
    private WarningRuleDTO testRuleDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testRule = new WarningRule();
        testRule.setId(1L);
        testRule.setRuleName("车辆故障预警规则");
        testRule.setRuleCode("VEHICLE_FAULT_001");
        testRule.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRule.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRule.setThresholdValue("{\"temperature\": 90}");
        testRule.setPushRoles("ROLE_SAFETY_OFFICER,ROLE_REPAIRMAN");
        testRule.setRuleConfig("{\"checkInterval\": 60}");
        testRule.setStatus(1);
        testRule.setDescription("车辆故障预警规则描述");
        testRule.setCreateTime(LocalDateTime.now());
        testRule.setUpdateTime(LocalDateTime.now());

        testRuleDTO = new WarningRuleDTO();
        testRuleDTO.setRuleName("车辆故障预警规则");
        testRuleDTO.setRuleCode("VEHICLE_FAULT_001");
        testRuleDTO.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        testRuleDTO.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        testRuleDTO.setThresholdValue("{\"temperature\": 90}");
        testRuleDTO.setDescription("车辆故障预警规则描述");
    }

    @AfterEach
    void tearDown() {
        reset(warningRuleMapper);
    }

    /**
     * 测试创建预警规则
     */
    @Test
    void testCreateWarningRule() {
        // Mock规则编码不存在
        when(warningRuleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // Mock数据库插入
        when(warningRuleMapper.insert(any(WarningRule.class))).thenAnswer(invocation -> {
            WarningRule rule = invocation.getArgument(0);
            rule.setId(1L);
            return 1;
        });

        // 执行测试
        Long result = warningRuleService.create(testRuleDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
        verify(warningRuleMapper, times(1)).insert(any(WarningRule.class));
    }

    /**
     * 测试创建预警规则时编码已存在
     */
    @Test
    void testCreateWarningRuleCodeExists() {
        // Mock规则编码已存在
        when(warningRuleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试并验证异常
        assertThrows(WarningException.class, () -> {
            warningRuleService.create(testRuleDTO);
        });

        // 验证未执行插入操作
        verify(warningRuleMapper, never()).insert(any(WarningRule.class));
    }

    /**
     * 测试更新预警规则
     */
    @Test
    void testUpdateWarningRule() {
        // Mock查询
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(warningRuleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(warningRuleMapper.updateById(any(WarningRule.class))).thenReturn(1);

        // 准备更新DTO
        testRuleDTO.setRuleName("更新后的规则名称");
        testRuleDTO.setRuleCode("VEHICLE_FAULT_002");

        // 执行测试
        warningRuleService.update(1L, testRuleDTO);

        // 验证更新调用
        verify(warningRuleMapper, times(1)).updateById(any(WarningRule.class));
    }

    /**
     * 测试更新不存在的预警规则
     */
    @Test
    void testUpdateWarningRuleNotFound() {
        when(warningRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRuleService.update(999L, testRuleDTO);
        });
    }

    /**
     * 测试更新预警规则时编码已被其他规则使用
     */
    @Test
    void testUpdateWarningRuleCodeExists() {
        // Mock查询
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        // Mock新编码已被其他规则使用
        when(warningRuleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 准备更新DTO（使用不同的编码）
        testRuleDTO.setRuleCode("VEHICLE_FAULT_002");

        assertThrows(WarningException.class, () -> {
            warningRuleService.update(1L, testRuleDTO);
        });
    }

    /**
     * 测试根据ID获取预警规则
     */
    @Test
    void testGetWarningRuleById() {
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);

        WarningRuleVO result = warningRuleService.getById(1L);

        assertNotNull(result);
        assertEquals("VEHICLE_FAULT_001", result.getRuleCode());
        assertEquals("车辆故障预警规则", result.getRuleName());
        assertEquals("车辆故障", result.getWarningTypeName());
        assertEquals("高风险", result.getWarningLevelName());
    }

    /**
     * 测试获取不存在的预警规则
     */
    @Test
    void testGetWarningRuleByIdNotFound() {
        when(warningRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRuleService.getById(999L);
        });
    }

    /**
     * 测试根据编码获取预警规则
     */
    @Test
    void testGetWarningRuleByCode() {
        when(warningRuleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRule);

        WarningRuleVO result = warningRuleService.getByCode("VEHICLE_FAULT_001");

        assertNotNull(result);
        assertEquals("VEHICLE_FAULT_001", result.getRuleCode());
        assertEquals("车辆故障预警规则", result.getRuleName());
    }

    /**
     * 测试根据编码获取不存在的预警规则
     */
    @Test
    void testGetWarningRuleByCodeNotFound() {
        when(warningRuleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        WarningRuleVO result = warningRuleService.getByCode("NOT_EXIST_CODE");

        assertNull(result);
    }

    /**
     * 测试启用预警规则
     */
    @Test
    void testEnableRule() {
        testRule.setStatus(0); // 当前为禁用状态
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(warningRuleMapper.updateById(any(WarningRule.class))).thenReturn(1);

        warningRuleService.enable(1L);

        verify(warningRuleMapper, times(1)).updateById(any(WarningRule.class));
    }

    /**
     * 测试启用不存在的预警规则
     */
    @Test
    void testEnableRuleNotFound() {
        when(warningRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRuleService.enable(999L);
        });
    }

    /**
     * 测试禁用预警规则
     */
    @Test
    void testDisableRule() {
        testRule.setStatus(1); // 当前为启用状态
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(warningRuleMapper.updateById(any(WarningRule.class))).thenReturn(1);

        warningRuleService.disable(1L);

        verify(warningRuleMapper, times(1)).updateById(any(WarningRule.class));
    }

    /**
     * 测试禁用不存在的预警规则
     */
    @Test
    void testDisableRuleNotFound() {
        when(warningRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRuleService.disable(999L);
        });
    }

    /**
     * 测试删除预警规则
     */
    @Test
    void testDeleteWarningRule() {
        when(warningRuleMapper.selectById(1L)).thenReturn(testRule);
        when(warningRuleMapper.deleteById(1L)).thenReturn(1);

        warningRuleService.delete(1L);

        verify(warningRuleMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试删除不存在的预警规则
     */
    @Test
    void testDeleteWarningRuleNotFound() {
        when(warningRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(WarningException.class, () -> {
            warningRuleService.delete(999L);
        });
    }

    /**
     * 测试获取所有启用的预警规则
     */
    @Test
    void testListAllRules() {
        when(warningRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testRule));

        List<WarningRuleVO> result = warningRuleService.listAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("VEHICLE_FAULT_001", result.get(0).getRuleCode());
    }

    /**
     * 测试获取所有启用的预警规则为空
     */
    @Test
    void testListAllRulesEmpty() {
        when(warningRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<WarningRuleVO> result = warningRuleService.listAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试分页查询预警规则
     */
    @Test
    void testPageWarningRules() {
        Page<WarningRule> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(testRule));
        page.setTotal(1);

        when(warningRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<WarningRuleVO> result = warningRuleService.page(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    /**
     * 测试分页查询为空的情况
     */
    @Test
    void testPageWarningRulesEmpty() {
        Page<WarningRule> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(warningRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        PageResult<WarningRuleVO> result = warningRuleService.page(pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
