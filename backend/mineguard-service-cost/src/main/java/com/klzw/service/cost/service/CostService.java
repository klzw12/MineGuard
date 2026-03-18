package com.klzw.service.cost.service;

import com.klzw.service.cost.entity.CostDetail;

import java.time.LocalDateTime;
import java.util.List;

public interface CostService {

    /**
     * 添加成本明细
     * @param costDetail 成本明细
     * @return 成本明细
     */
    CostDetail addCostDetail(CostDetail costDetail);

    /**
     * 更新成本明细
     * @param costDetail 成本明细
     * @return 成本明细
     */
    CostDetail updateCostDetail(CostDetail costDetail);

    /**
     * 删除成本明细
     * @param id 成本明细ID
     * @return 是否删除成功
     */
    boolean deleteCostDetail(Long id);

    /**
     * 获取成本明细
     * @param id 成本明细ID
     * @return 成本明细
     */
    CostDetail getCostDetail(Long id);

    /**
     * 获取成本明细列表
     * @param vehicleId 车辆ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本明细列表
     */
    List<CostDetail> getCostDetailList(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计成本
     * @param vehicleId 车辆ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本统计结果
     */
    Object getCostStatistics(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
}