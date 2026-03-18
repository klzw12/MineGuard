package com.klzw.service.statistics.controller;

import com.klzw.service.statistics.entity.TripStatistics;
import com.klzw.service.statistics.entity.CostStatistics;
import com.klzw.service.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取行程统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程统计数据列表
     */
    @GetMapping("/trip")
    public List<TripStatistics> getTripStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            log.debug("获取行程统计数据：开始日期={}, 结束日期={}", startDate, endDate);
            return statisticsService.getTripStatistics(start, end);
        } catch (Exception e) {
            log.error("获取行程统计数据异常", e);
            throw e;
        }
    }

    /**
     * 获取成本统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本统计数据列表
     */
    @GetMapping("/cost")
    public List<CostStatistics> getCostStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            log.debug("获取成本统计数据：开始日期={}, 结束日期={}", startDate, endDate);
            return statisticsService.getCostStatistics(start, end);
        } catch (Exception e) {
            log.error("获取成本统计数据异常", e);
            throw e;
        }
    }

    /**
     * 获取总体统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总体统计数据
     */
    @GetMapping("/overall")
    public Object getOverallStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            log.debug("获取总体统计数据：开始日期={}, 结束日期={}", startDate, endDate);
            return statisticsService.getOverallStatistics(start, end);
        } catch (Exception e) {
            log.error("获取总体统计数据异常", e);
            throw e;
        }
    }

    /**
     * 计算并保存行程统计数据
     * @param date 统计日期
     * @return 行程统计数据
     */
    @GetMapping("/trip/calculate")
    public TripStatistics calculateTripStatistics(@RequestParam("date") String date) {
        try {
            LocalDate statisticsDate = LocalDate.parse(date);
            log.debug("计算行程统计数据：日期={}", date);
            return statisticsService.calculateTripStatistics(statisticsDate);
        } catch (Exception e) {
            log.error("计算行程统计数据异常", e);
            throw e;
        }
    }

    /**
     * 计算并保存成本统计数据
     * @param date 统计日期
     * @return 成本统计数据
     */
    @GetMapping("/cost/calculate")
    public CostStatistics calculateCostStatistics(@RequestParam("date") String date) {
        try {
            LocalDate statisticsDate = LocalDate.parse(date);
            log.debug("计算成本统计数据：日期={}", date);
            return statisticsService.calculateCostStatistics(statisticsDate);
        } catch (Exception e) {
            log.error("计算成本统计数据异常", e);
            throw e;
        }
    }
}