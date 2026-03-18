package com.klzw.service.cost.controller;

import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.service.CostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    /**
     * 添加成本明细
     * @param costDetail 成本明细
     * @return 成本明细
     */
    @PostMapping
    public CostDetail addCostDetail(@RequestBody CostDetail costDetail) {
        try {
            log.debug("添加成本明细：{}", costDetail);
            return costService.addCostDetail(costDetail);
        } catch (Exception e) {
            log.error("添加成本明细异常", e);
            throw e;
        }
    }

    /**
     * 更新成本明细
     * @param costDetail 成本明细
     * @return 成本明细
     */
    @PutMapping
    public CostDetail updateCostDetail(@RequestBody CostDetail costDetail) {
        try {
            log.debug("更新成本明细：{}", costDetail);
            return costService.updateCostDetail(costDetail);
        } catch (Exception e) {
            log.error("更新成本明细异常", e);
            throw e;
        }
    }

    /**
     * 删除成本明细
     * @param id 成本明细ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public boolean deleteCostDetail(@PathVariable Long id) {
        try {
            log.debug("删除成本明细：ID={}", id);
            return costService.deleteCostDetail(id);
        } catch (Exception e) {
            log.error("删除成本明细异常", e);
            throw e;
        }
    }

    /**
     * 获取成本明细
     * @param id 成本明细ID
     * @return 成本明细
     */
    @GetMapping("/{id}")
    public CostDetail getCostDetail(@PathVariable Long id) {
        try {
            log.debug("获取成本明细：ID={}", id);
            return costService.getCostDetail(id);
        } catch (Exception e) {
            log.error("获取成本明细异常", e);
            throw e;
        }
    }

    /**
     * 获取成本明细列表
     * @param vehicleId 车辆ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本明细列表
     */
    @GetMapping("/list")
    public List<CostDetail> getCostDetailList(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            log.debug("获取成本明细列表：车辆ID={}, 开始日期={}, 结束日期={}", 
                    vehicleId, startDate, endDate);
            return costService.getCostDetailList(vehicleId, start, end);
        } catch (Exception e) {
            log.error("获取成本明细列表异常", e);
            throw e;
        }
    }

    /**
     * 统计成本
     * @param vehicleId 车辆ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本统计结果
     */
    @GetMapping("/statistics")
    public Object getCostStatistics(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            log.debug("统计成本：车辆ID={}, 开始日期={}, 结束日期={}", 
                    vehicleId, startDate, endDate);
            return costService.getCostStatistics(vehicleId, start, end);
        } catch (Exception e) {
            log.error("统计成本异常", e);
            throw e;
        }
    }
}