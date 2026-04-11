package com.klzw.service.cost.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.client.TransportClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.dto.SalaryConfigDTO;
import com.klzw.service.cost.dto.SalaryRecordDTO;
import com.klzw.service.cost.entity.CostBudget;
import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.entity.SalaryConfig;
import com.klzw.service.cost.entity.SalaryRecord;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.BudgetTypeEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.mapper.CostBudgetMapper;
import com.klzw.service.cost.mapper.CostDetailMapper;
import com.klzw.service.cost.mapper.SalaryConfigMapper;
import com.klzw.service.cost.mapper.SalaryRecordMapper;
import com.klzw.service.cost.service.impl.CostServiceImpl;
import com.klzw.service.cost.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class CostServiceImplTest {

    @Mock
    private CostDetailMapper costDetailMapper;

    @Mock
    private SalaryConfigMapper salaryConfigMapper;

    @Mock
    private SalaryRecordMapper salaryRecordMapper;

    @Mock
    private CostBudgetMapper costBudgetMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private PythonClient pythonClient;

    @Mock
    private TransportClient transportClient;

    @Mock
    private TripClient tripClient;

    @Mock
    private VehicleClient vehicleClient;

    @InjectMocks
    private CostServiceImpl costService;

    private CostDetailDTO costDetailDTO;
    private CostQueryDTO costQueryDTO;
    private SalaryConfigDTO salaryConfigDTO;
    private SalaryRecordDTO salaryRecordDTO;
    private CostBudgetDTO costBudgetDTO;
    private CostDetail costDetail;
    private SalaryConfig salaryConfig;
    private SalaryRecord salaryRecord;
    private CostBudget costBudget;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        costDetailDTO = new CostDetailDTO();
        costDetailDTO.setCostType(CostTypeEnum.FUEL.getCode());
        costDetailDTO.setAmount(new BigDecimal(1000));
        costDetailDTO.setCostDate(LocalDate.now());
        costDetailDTO.setVehicleId(1L);
        costDetailDTO.setUserId(1L);
        costDetailDTO.setDescription("测试燃油费用");

        costQueryDTO = new CostQueryDTO();
        costQueryDTO.setStartDate(LocalDate.now().minusDays(7));
        costQueryDTO.setEndDate(LocalDate.now());
        costQueryDTO.setCostType(CostTypeEnum.FUEL.getCode());

        salaryConfigDTO = new SalaryConfigDTO();
        salaryConfigDTO.setRoleCode("DRIVER");
        salaryConfigDTO.setRoleName("司机");
        salaryConfigDTO.setBaseSalary(new BigDecimal(5000));
        salaryConfigDTO.setPerformanceBonus(new BigDecimal(0.1));
        salaryConfigDTO.setStatus(1);

        salaryRecordDTO = new SalaryRecordDTO();
        salaryRecordDTO.setDriverId(1L);
        salaryRecordDTO.setDriverName("张三");
        salaryRecordDTO.setPeriod("2024-01");
        salaryRecordDTO.setBaseSalary(new BigDecimal(5000));
        salaryRecordDTO.setBonus(new BigDecimal(500));
        salaryRecordDTO.setDeduction(new BigDecimal(200));

        costBudgetDTO = new CostBudgetDTO();
        costBudgetDTO.setBudgetName("2024年燃油预算");
        costBudgetDTO.setBudgetType(BudgetTypeEnum.MONTHLY.getCode());
        costBudgetDTO.setBudgetYear(2024);
        costBudgetDTO.setBudgetMonth(1);
        costBudgetDTO.setFuelBudget(new BigDecimal(10000));
        costBudgetDTO.setMaintenanceBudget(new BigDecimal(5000));
        costBudgetDTO.setLaborBudget(new BigDecimal(20000));
        costBudgetDTO.setBudgetYear(2024);
        costBudgetDTO.setBudgetMonth(1);

        costDetail = new CostDetail();
        costDetail.setId(1L);
        costDetail.setCostNo("COST202401010001");
        costDetail.setCostType(CostTypeEnum.FUEL.getCode());
        costDetail.setCostName("燃油费用");
        costDetail.setAmount(new BigDecimal(1000));
        costDetail.setCostDate(LocalDate.now());
        costDetail.setVehicleId(1L);
        costDetail.setUserId(1L);
        costDetail.setRemark("测试燃油费用");
        costDetail.setCreateTime(LocalDateTime.now());
        costDetail.setUpdateTime(LocalDateTime.now());
        costDetail.setDeleted(0);

        salaryConfig = new SalaryConfig();
        salaryConfig.setId(1L);
        salaryConfig.setRoleCode("DRIVER");
        salaryConfig.setRoleName("司机");
        salaryConfig.setBaseSalary(new BigDecimal(5000));
        salaryConfig.setPerformanceBonus(new BigDecimal(0.1));
        salaryConfig.setStatus(1);
        salaryConfig.setCreateTime(LocalDateTime.now());
        salaryConfig.setUpdateTime(LocalDateTime.now());
        salaryConfig.setDeleted(0);

        salaryRecord = new SalaryRecord();
        salaryRecord.setId(1L);
        salaryRecord.setDriverId(1L);
        salaryRecord.setDriverName("张三");
        salaryRecord.setPeriod("2024-01");
        salaryRecord.setBaseSalary(new BigDecimal(5000));
        salaryRecord.setBonus(new BigDecimal(500));
        salaryRecord.setDeduction(new BigDecimal(200));
        salaryRecord.setTotalSalary(new BigDecimal(5300));
        salaryRecord.setStatus(1);
        salaryRecord.setCreateTime(LocalDateTime.now());
        salaryRecord.setUpdateTime(LocalDateTime.now());
        salaryRecord.setDeleted(0);

        costBudget = new CostBudget();
        costBudget.setId(1L);
        costBudget.setBudgetNo("BUDGET202401010001");
        costBudget.setBudgetName("2024年燃油预算");
        costBudget.setBudgetType(BudgetTypeEnum.MONTHLY.getCode());
        costBudget.setBudgetYear(2024);
        costBudget.setBudgetMonth(1);
        costBudget.setFuelBudget(new BigDecimal(10000));
        costBudget.setMaintenanceBudget(new BigDecimal(5000));
        costBudget.setLaborBudget(new BigDecimal(20000));
        costBudget.setTotalBudget(new BigDecimal(35000));
        costBudget.setStatus(BudgetStatusEnum.ACTIVE.getCode());
        costBudget.setStartDate(LocalDate.of(2024, 1, 1));
        costBudget.setEndDate(LocalDate.of(2024, 1, 31));
        costBudget.setCreateTime(LocalDateTime.now());
        costBudget.setUpdateTime(LocalDateTime.now());
        costBudget.setDeleted(0);

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testAddCostDetail() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.insert(any(CostDetail.class))).thenReturn(1);

        // 调用被测方法
        CostDetailVO result = costService.addCostDetail(costDetailDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getCostNo());
        assertEquals(CostTypeEnum.FUEL.getCode(), result.getCostType());
        assertEquals(new BigDecimal(1000), result.getAmount());
        assertEquals("测试燃油费用", result.getDescription());

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }

    @Test
    void testUpdateCostDetail() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectById(anyLong())).thenReturn(costDetail);
        when(costDetailMapper.updateById(any(CostDetail.class))).thenReturn(1);

        // 调用被测方法
        costDetailDTO.setId(1L);
        costDetailDTO.setAmount(new BigDecimal(1500));
        CostDetailVO result = costService.updateCostDetail(costDetailDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal(1500), result.getAmount());

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectById(anyLong());
        verify(costDetailMapper, times(1)).updateById(any(CostDetail.class));
    }

    @Test
    void testUpdateCostDetailWithException() {
        // 模拟依赖方法返回null
        when(costDetailMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        costDetailDTO.setId(1L);
        assertThrows(RuntimeException.class, () -> costService.updateCostDetail(costDetailDTO));

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testDeleteCostDetail() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.deleteById(anyLong())).thenReturn(1);

        // 调用被测方法
        costService.deleteCostDetail(1L);

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).deleteById(anyLong());
    }

    @Test
    void testGetCostDetail() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectById(anyLong())).thenReturn(costDetail);

        // 调用被测方法
        CostDetailVO result = costService.getCostDetail(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("COST202401010001", result.getCostNo());
        assertEquals(new BigDecimal(1000), result.getAmount());

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetCostDetailWithException() {
        // 模拟依赖方法返回null
        when(costDetailMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        assertThrows(RuntimeException.class, () -> costService.getCostDetail(1L));

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetCostDetailList() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        List<CostDetailVO> result = costService.getCostDetailList(costQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(new BigDecimal(1000), result.get(0).getAmount());

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetCostStatistics() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        CostStatisticsVO result = costService.getCostStatistics(costQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal(1000), result.getTotalAmount());
        assertEquals(1, result.getRecordCount());
        assertEquals(new BigDecimal(1000), result.getFuelCost());

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testAddSalaryConfig() {
        // 模拟依赖方法的返回值
        when(salaryConfigMapper.insert(any(SalaryConfig.class))).thenReturn(1);

        // 调用被测方法
        SalaryConfigVO result = costService.addSalaryConfig(salaryConfigDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("DRIVER", result.getRoleCode());
        assertEquals("司机", result.getRoleName());
        assertEquals(new BigDecimal(5000), result.getBaseSalary());

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).insert(any(SalaryConfig.class));
    }

    @Test
    void testUpdateSalaryConfig() {
        // 模拟依赖方法的返回值
        when(salaryConfigMapper.selectById(anyLong())).thenReturn(salaryConfig);
        when(salaryConfigMapper.updateById(any(SalaryConfig.class))).thenReturn(1);

        // 调用被测方法
        salaryConfigDTO.setId(1L);
        salaryConfigDTO.setBaseSalary(new BigDecimal(6000));
        SalaryConfigVO result = costService.updateSalaryConfig(salaryConfigDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal(6000), result.getBaseSalary());

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).selectById(anyLong());
        verify(salaryConfigMapper, times(1)).updateById(any(SalaryConfig.class));
    }

    @Test
    void testUpdateSalaryConfigWithException() {
        // 模拟依赖方法返回null
        when(salaryConfigMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        salaryConfigDTO.setId(1L);
        assertThrows(RuntimeException.class, () -> costService.updateSalaryConfig(salaryConfigDTO));

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testDeleteSalaryConfig() {
        // 模拟依赖方法的返回值
        when(salaryConfigMapper.deleteById(anyLong())).thenReturn(1);

        // 调用被测方法
        costService.deleteSalaryConfig(1L);

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).deleteById(anyLong());
    }

    @Test
    void testGetSalaryConfig() {
        // 模拟依赖方法的返回值
        when(salaryConfigMapper.selectById(anyLong())).thenReturn(salaryConfig);

        // 调用被测方法
        SalaryConfigVO result = costService.getSalaryConfig(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DRIVER", result.getRoleCode());
        assertEquals("司机", result.getRoleName());

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetSalaryConfigWithException() {
        // 模拟依赖方法返回null
        when(salaryConfigMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        assertThrows(RuntimeException.class, () -> costService.getSalaryConfig(1L));

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetSalaryConfigList() {
        // 模拟依赖方法的返回值
        when(salaryConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(salaryConfig));

        // 调用被测方法
        List<SalaryConfigVO> result = costService.getSalaryConfigList();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("DRIVER", result.get(0).getRoleCode());

        // 验证依赖方法被调用
        verify(salaryConfigMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testAddSalaryRecord() {
        // 模拟依赖方法的返回值
        when(salaryRecordMapper.insert(any(SalaryRecord.class))).thenReturn(1);

        // 调用被测方法
        SalaryRecordVO result = costService.addSalaryRecord(salaryRecordDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getDriverId());
        assertEquals("张三", result.getDriverName());
        assertEquals("2024-01", result.getPeriod());
        assertEquals(new BigDecimal(5300), result.getTotalSalary());

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).insert(any(SalaryRecord.class));
    }

    @Test
    void testUpdateSalaryRecord() {
        // 模拟依赖方法的返回值
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(salaryRecord);
        when(salaryRecordMapper.updateById(any(SalaryRecord.class))).thenReturn(1);

        // 调用被测方法
        salaryRecordDTO.setId(1L);
        salaryRecordDTO.setBonus(new BigDecimal(800));
        SalaryRecordVO result = costService.updateSalaryRecord(salaryRecordDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal(800), result.getBonus());

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
        verify(salaryRecordMapper, times(1)).updateById(any(SalaryRecord.class));
    }

    @Test
    void testUpdateSalaryRecordWithException() {
        // 模拟依赖方法返回null
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        salaryRecordDTO.setId(1L);
        assertThrows(RuntimeException.class, () -> costService.updateSalaryRecord(salaryRecordDTO));

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testDeleteSalaryRecord() {
        // 模拟依赖方法的返回值
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(salaryRecord);
        when(salaryRecordMapper.updateById(any(SalaryRecord.class))).thenReturn(1);

        // 调用被测方法
        costService.deleteSalaryRecord(1L);

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
        verify(salaryRecordMapper, times(1)).updateById(any(SalaryRecord.class));
    }

    @Test
    void testDeleteSalaryRecordWithException() {
        // 模拟依赖方法返回null
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        assertThrows(RuntimeException.class, () -> costService.deleteSalaryRecord(1L));

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetSalaryRecord() {
        // 模拟依赖方法的返回值
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(salaryRecord);

        // 调用被测方法
        SalaryRecordVO result = costService.getSalaryRecord(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("张三", result.getDriverName());
        assertEquals("2024-01", result.getPeriod());

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetSalaryRecordWithNull() {
        // 模拟依赖方法返回null
        when(salaryRecordMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法
        SalaryRecordVO result = costService.getSalaryRecord(1L);

        // 验证结果
        assertNull(result);

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetSalaryRecordList() {
        // 模拟依赖方法的返回值
        when(salaryRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(salaryRecord));

        // 调用被测方法
        List<SalaryRecordVO> result = costService.getSalaryRecordList("张三", "2024-01", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("张三", result.get(0).getDriverName());

        // 验证依赖方法被调用
        verify(salaryRecordMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testAddBudget() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.insert(any(CostBudget.class))).thenReturn(1);

        // 调用被测方法
        CostBudgetVO result = costService.addBudget(costBudgetDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getBudgetNo());
        assertEquals("2024年燃油预算", result.getBudgetName());
        assertEquals(new BigDecimal(35000), result.getTotalBudget());

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).insert(any(CostBudget.class));
    }

    @Test
    void testUpdateBudget() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.selectById(anyLong())).thenReturn(costBudget);
        when(costBudgetMapper.updateById(any(CostBudget.class))).thenReturn(1);

        // 调用被测方法
        costBudgetDTO.setId(1L);
        costBudgetDTO.setFuelBudget(new BigDecimal(15000));
        CostBudgetVO result = costService.updateBudget(costBudgetDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal(15000), result.getFuelBudget());

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
        verify(costBudgetMapper, times(1)).updateById(any(CostBudget.class));
    }

    @Test
    void testUpdateBudgetWithException() {
        // 模拟依赖方法返回null
        when(costBudgetMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        costBudgetDTO.setId(1L);
        assertThrows(RuntimeException.class, () -> costService.updateBudget(costBudgetDTO));

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testDeleteBudget() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.deleteById(anyLong())).thenReturn(1);

        // 调用被测方法
        costService.deleteBudget(1L);

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).deleteById(anyLong());
    }

    @Test
    void testGetBudget() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.selectById(anyLong())).thenReturn(costBudget);

        // 调用被测方法
        CostBudgetVO result = costService.getBudget(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("2024年燃油预算", result.getBudgetName());
        assertEquals(new BigDecimal(35000), result.getTotalBudget());

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetBudgetWithException() {
        // 模拟依赖方法返回null
        when(costBudgetMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        assertThrows(RuntimeException.class, () -> costService.getBudget(1L));

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGetBudgetList() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costBudget));

        // 调用被测方法
        List<CostBudgetVO> result = costService.getBudgetList(BudgetTypeEnum.MONTHLY.getCode(), 2024);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("2024年燃油预算", result.get(0).getBudgetName());

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testCheckBudgetUsage() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.selectById(anyLong())).thenReturn(costBudget);
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        Map<String, Object> result = costService.checkBudgetUsage(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.get("budgetId"));
        assertEquals("2024年燃油预算", result.get("budgetName"));
        assertEquals(new BigDecimal(35000), result.get("totalBudget"));
        assertEquals(new BigDecimal(1000), result.get("usedAmount"));
        assertEquals(new BigDecimal(34000), result.get("remainingAmount"));

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testCheckBudgetUsageWithException() {
        // 模拟依赖方法返回null
        when(costBudgetMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法，验证异常
        assertThrows(RuntimeException.class, () -> costService.checkBudgetUsage(1L));

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testGenerateCostReport() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        Map<String, Object> result = costService.generateCostReport(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal(1000), result.get("totalAmount"));
        assertEquals(1, result.get("recordCount"));
        assertEquals(new BigDecimal(1000), result.get("fuelCost"));

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetBudgetAlerts() {
        // 模拟依赖方法的返回值
        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costBudget));
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // 调用被测方法
        List<Map<String, Object>> result = costService.getBudgetAlerts();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证依赖方法被调用
        verify(costBudgetMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetCostTrend() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        Map<String, Object> result = costService.getCostTrend(LocalDate.now().minusDays(7), LocalDate.now(), "day");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.get("trendData"));
        assertNotNull(result.get("labels"));
        assertNotNull(result.get("values"));

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetEnergyConsumptionAnalysis() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));

        // 调用被测方法
        Map<String, Object> result = costService.getEnergyConsumptionAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal(1000), result.get("totalFuelCost"));

        // 验证依赖方法被调用
        verify(costDetailMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetVehicleUtilizationAnalysis() {
        // 调用被测方法
        Map<String, Object> result = costService.getVehicleUtilizationAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
    }

    @Test
    void testGetIdleRateAnalysis() {
        // 调用被测方法
        Map<String, Object> result = costService.getIdleRateAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
    }

    @Test
    void testGetOverallCostAnalysis() {
        // 模拟依赖方法的返回值
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costDetail));
        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costBudget));

        // 调用被测方法
        Map<String, Object> result = costService.getOverallCostAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal(1000), result.get("totalCost"));

        // 验证依赖方法被调用
        verify(costDetailMapper, atLeast(1)).selectList(any(LambdaQueryWrapper.class));
        verify(costBudgetMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testCalculateAndRecordTripCommission() {
        // 模拟依赖方法的返回值
        when(pythonClient.analyzeDrivingBehavior(anyLong())).thenReturn(85);
        when(costDetailMapper.insert(any(CostDetail.class))).thenAnswer(invocation -> {
            CostDetail detail = invocation.getArgument(0);
            detail.setId(100L);
            return 1;
        });

        // 调用被测方法
        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000);

        // 验证结果
        assertNotNull(result);

        // 验证依赖方法被调用
        verify(pythonClient, times(1)).analyzeDrivingBehavior(anyLong());
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }

    @Test
    void testCalculateAndRecordTripCommissionWithException() {
        // 模拟依赖方法抛出异常
        when(pythonClient.analyzeDrivingBehavior(anyLong())).thenThrow(new RuntimeException("测试异常"));
        when(costDetailMapper.insert(any(CostDetail.class))).thenAnswer(invocation -> {
            CostDetail detail = invocation.getArgument(0);
            detail.setId(100L);
            return 1;
        });

        // 调用被测方法
        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000);

        // 验证结果
        assertNotNull(result);

        // 验证依赖方法被调用
        verify(pythonClient, times(1)).analyzeDrivingBehavior(anyLong());
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }

    @Test
    void testCalculateAndRecordTripCommissionWithSaveException() {
        // 模拟依赖方法的返回值
        when(pythonClient.analyzeDrivingBehavior(anyLong())).thenReturn(85);
        when(costDetailMapper.insert(any(CostDetail.class))).thenThrow(new RuntimeException("保存异常"));

        // 调用被测方法
        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000);

        // 验证结果
        assertNull(result);

        // 验证依赖方法被调用
        verify(pythonClient, times(1)).analyzeDrivingBehavior(anyLong());
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }
}
