package com.klzw.service.cost.service.impl;

import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.service.CostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostServiceImpl implements CostService {

    private final Random random = new Random();

    @Override
    public CostDetail addCostDetail(CostDetail costDetail) {
        costDetail.setCostNo(generateCostNo());
        costDetail.setCreateTime(LocalDateTime.now());
        costDetail.setUpdateTime(LocalDateTime.now());
        
        log.info("添加成本明细：编号={}, 类型={}, 金额={}", 
                costDetail.getCostNo(), costDetail.getCostType(), costDetail.getAmount());
        
        // 这里应该使用MyBatis-Plus或其他ORM框架保存到数据库
        // 暂时返回模拟数据
        return costDetail;
    }

    @Override
    public CostDetail updateCostDetail(CostDetail costDetail) {
        costDetail.setUpdateTime(LocalDateTime.now());
        
        log.info("更新成本明细：ID={}, 金额={}", 
                costDetail.getId(), costDetail.getAmount());
        
        // 这里应该使用MyBatis-Plus或其他ORM框架更新到数据库
        // 暂时返回模拟数据
        return costDetail;
    }

    @Override
    public boolean deleteCostDetail(Long id) {
        log.info("删除成本明细：ID={}", id);
        
        // 这里应该使用MyBatis-Plus或其他ORM框架从数据库删除
        // 暂时返回模拟数据
        return true;
    }

    @Override
    public CostDetail getCostDetail(Long id) {
        CostDetail costDetail = new CostDetail();
        costDetail.setId(id);
        costDetail.setCostNo("COST" + System.currentTimeMillis());
        costDetail.setCostType(1);
        costDetail.setCostName("燃油费");
        costDetail.setAmount(new BigDecimal(200));
        costDetail.setVehicleId(1L);
        costDetail.setTripId(1L);
        costDetail.setDescription("车辆加油");
        costDetail.setPaymentMethod("现金");
        costDetail.setCostDate(LocalDateTime.now());
        costDetail.setCreateTime(LocalDateTime.now());
        costDetail.setUpdateTime(LocalDateTime.now());
        
        log.debug("获取成本明细：ID={}", id);
        
        // 这里应该使用MyBatis-Plus或其他ORM框架从数据库查询
        // 暂时返回模拟数据
        return costDetail;
    }

    @Override
    public List<CostDetail> getCostDetailList(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        List<CostDetail> costDetailList = new ArrayList<>();
        
        // 模拟数据
        for (int i = 0; i < 5; i++) {
            CostDetail costDetail = new CostDetail();
            costDetail.setId((long) (i + 1));
            costDetail.setCostNo("COST" + (System.currentTimeMillis() + i));
            costDetail.setCostType(i % 4 + 1);
            costDetail.setCostName(getCostName(i % 4 + 1));
            costDetail.setAmount(new BigDecimal(100 + i * 50));
            costDetail.setVehicleId(vehicleId);
            costDetail.setTripId((long) (i + 1));
            costDetail.setDescription("成本描述" + i);
            costDetail.setPaymentMethod(i % 2 == 0 ? "现金" : "微信");
            costDetail.setCostDate(LocalDateTime.now().minusDays(i));
            costDetail.setCreateTime(LocalDateTime.now());
            costDetail.setUpdateTime(LocalDateTime.now());
            costDetailList.add(costDetail);
        }
        
        log.debug("获取成本明细列表：车辆ID={}, 开始日期={}, 结束日期={}", 
                vehicleId, startDate, endDate);
        
        // 这里应该使用MyBatis-Plus或其他ORM框架从数据库查询
        // 暂时返回模拟数据
        return costDetailList;
    }

    @Override
    public Object getCostStatistics(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        List<CostDetail> costDetailList = getCostDetailList(vehicleId, startDate, endDate);
        
        // 统计成本
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> typeAmountMap = new HashMap<>();
        
        for (CostDetail costDetail : costDetailList) {
            totalAmount = totalAmount.add(costDetail.getAmount());
            BigDecimal typeAmount = typeAmountMap.getOrDefault(costDetail.getCostType(), BigDecimal.ZERO);
            typeAmount = typeAmount.add(costDetail.getAmount());
            typeAmountMap.put(costDetail.getCostType(), typeAmount);
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalAmount", totalAmount);
        result.put("typeAmountMap", typeAmountMap);
        result.put("costCount", costDetailList.size());
        
        log.debug("统计成本：车辆ID={}, 总成本={}", vehicleId, totalAmount);
        
        return result;
    }

    /**
     * 生成成本编号
     * @return 成本编号
     */
    private String generateCostNo() {
        return "COST" + System.currentTimeMillis() + random.nextInt(1000);
    }

    /**
     * 根据成本类型获取成本名称
     * @param costType 成本类型
     * @return 成本名称
     */
    private String getCostName(int costType) {
        switch (costType) {
            case 1:
                return "燃油费";
            case 2:
                return "维修费";
            case 3:
                return "人工费";
            case 4:
                return "其他费用";
            default:
                return "未知费用";
        }
    }
}