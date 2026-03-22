package com.klzw.service.statistics.task;

import com.klzw.service.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduledTask {

    private final StatisticsService statisticsService;
    private final RestTemplate restTemplate;

    @Scheduled(cron = "0 5 0 * * ?")
    public void calculateDailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行每日统计任务，统计日期：{}", yesterday);
        
        try {
            statisticsService.calculateTripStatistics(yesterday.toString());
            log.info("行程统计计算完成：{}", yesterday);
        } catch (Exception e) {
            log.error("行程统计计算失败：{}", yesterday, e);
        }
        
        try {
            statisticsService.calculateCostStatistics(yesterday.toString());
            log.info("成本统计计算完成：{}", yesterday);
        } catch (Exception e) {
            log.error("成本统计计算失败：{}", yesterday, e);
        }
        
        log.info("每日统计任务执行完成");
    }

    @Scheduled(cron = "0 0 1 1 * ?")
    public void calculateMonthlyStatistics() {
        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(
                LocalDate.now().minusMonths(1).lengthOfMonth());
        
        log.info("开始执行月度统计汇总任务，统计月份：{} 至 {}", firstDayOfLastMonth, lastDayOfLastMonth);
        
        try {
            statisticsService.calculateMonthlyTripStatistics(firstDayOfLastMonth, lastDayOfLastMonth);
            log.info("月度行程统计汇总完成");
        } catch (Exception e) {
            log.error("月度行程统计汇总失败", e);
        }
        
        try {
            statisticsService.calculateMonthlyCostStatistics(firstDayOfLastMonth, lastDayOfLastMonth);
            log.info("月度成本统计汇总完成");
        } catch (Exception e) {
            log.error("月度成本统计汇总失败", e);
        }
        
        log.info("月度统计汇总任务执行完成");
    }

    @Scheduled(cron = "0 10 0 * * ?")
    public void calculateVehicleStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行车辆统计任务，统计日期：{}", yesterday);
        
        try {
            String vehicleServiceUrl = "http://mineguard-service-vehicle/api/vehicle/ids";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(vehicleServiceUrl, Map.class);
            
            if (response != null && response.get("data") != null) {
                @SuppressWarnings("unchecked")
                List<Long> vehicleIds = (List<Long>) response.get("data");
                
                if (vehicleIds != null && !vehicleIds.isEmpty()) {
                    int successCount = 0;
                    int failCount = 0;
                    
                    for (Long vehicleId : vehicleIds) {
                        try {
                            statisticsService.calculateVehicleStatistics(vehicleId, yesterday.toString());
                            successCount++;
                        } catch (Exception e) {
                            log.error("车辆统计失败：车辆ID={}", vehicleId, e);
                            failCount++;
                        }
                    }
                    
                    log.info("车辆统计任务完成：成功={}, 失败={}", successCount, failCount);
                }
            }
        } catch (Exception e) {
            log.error("获取车辆列表失败", e);
        }
        
        log.info("车辆统计任务执行完成");
    }

    @Scheduled(cron = "0 15 0 * * ?")
    public void calculateDriverStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行司机统计任务，统计日期：{}", yesterday);
        
        try {
            String userServiceUrl = "http://mineguard-service-user/api/user/driver-ids";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(userServiceUrl, Map.class);
            
            if (response != null && response.get("data") != null) {
                @SuppressWarnings("unchecked")
                List<Long> driverIds = (List<Long>) response.get("data");
                
                if (driverIds != null && !driverIds.isEmpty()) {
                    int successCount = 0;
                    int failCount = 0;
                    
                    for (Long userId : driverIds) {
                        try {
                            statisticsService.calculateDriverStatistics(userId, yesterday.toString());
                            successCount++;
                        } catch (Exception e) {
                            log.error("司机统计失败：用户ID={}", userId, e);
                            failCount++;
                        }
                    }
                    
                    log.info("司机统计任务完成：成功={}, 失败={}", successCount, failCount);
                }
            }
        } catch (Exception e) {
            log.error("获取司机列表失败", e);
        }
        
        log.info("司机统计任务执行完成");
    }

    @Scheduled(cron = "0 30 0 * * ?")
    public void calculateTransportStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行运输统计任务，统计日期：{}", yesterday);
        
        try {
            statisticsService.calculateTransportStatistics(yesterday.toString());
            log.info("运输统计计算完成：{}", yesterday);
        } catch (Exception e) {
            log.error("运输统计计算失败：{}", yesterday, e);
        }
        
        log.info("运输统计任务执行完成");
    }
}
