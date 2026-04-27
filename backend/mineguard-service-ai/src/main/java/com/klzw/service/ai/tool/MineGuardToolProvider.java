package com.klzw.service.ai.tool;

import com.klzw.common.core.client.*;
import com.klzw.common.core.domain.dto.CostStatisticsResponseDTO;
import com.klzw.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MineGuardToolProvider {

    @Autowired
    private VehicleClient vehicleClient;

    @Autowired
    private TripClient tripClient;

    @Autowired
    private StatisticsClient statisticsClient;

    @Autowired
    private CostClient costClient;

    @Autowired
    private DispatchClient dispatchClient;

    @Autowired
    private WarningClient warningClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool(name = "get_vehicle_status", description = "获取今日车辆运行情况，包括总车辆数、空闲车辆数、故障车辆、维修车辆、今日行程数等统计信息")
    public Map<String, Object> getVehicleStatus() {
        log.info("[Tool] get_vehicle_status 被调用");
        Map<String, Object> result = new HashMap<>();
        String today = LocalDate.now().format(DATE_FORMATTER);
        
        try {
            Result<Integer> totalResult = vehicleClient.getVehicleCount();
            Integer totalVehicles = totalResult != null && totalResult.getCode() == 200 ? totalResult.getData() : 0;
            result.put("totalVehicles", totalVehicles);

            Result<List<java.util.Map<String, Object>>> idleResult = vehicleClient.getIdleVehicles();
            Integer idleCount = idleResult != null && idleResult.getCode() == 200 && idleResult.getData() != null ? idleResult.getData().size() : 0;
            result.put("idleVehicles", idleCount);

            Result<List<java.util.Map<String, Object>>> faultResult = vehicleClient.getFaultVehicles();
            Integer faultCount = faultResult != null && faultResult.getCode() == 200 && faultResult.getData() != null ? faultResult.getData().size() : 0;
            result.put("faultVehicles", faultCount);

            Result<List<java.util.Map<String, Object>>> maintenanceResult = vehicleClient.getMaintenanceVehicles();
            Integer maintenanceCount = maintenanceResult != null && maintenanceResult.getCode() == 200 && maintenanceResult.getData() != null ? maintenanceResult.getData().size() : 0;
            result.put("maintenanceVehicles", maintenanceCount);

            Map<String, Object> tripStats = tripClient.getStatistics(today, today).getData();
            if (tripStats != null) {
                result.put("todayTrips", tripStats.getOrDefault("totalTrips", 0));
                result.put("activeTrips", tripStats.getOrDefault("activeTrips", 0));
            }

            result.put("status", "success");
            result.put("message", "获取车辆运行情况成功");
            result.put("date", today);
            
        } catch (Exception e) {
            log.error("[Tool] get_vehicle_status 执行失败", e);
            result.put("status", "error");
            result.put("message", "获取车辆运行情况失败: " + e.getMessage());
        }
        
        return result;
    }

    @Tool(name = "get_cost_analysis", description = "获取成本分析报告，包括本月总成本、燃油成本、维护成本、人工成本等各项费用统计及优化建议")
    public Map<String, Object> getCostAnalysis(
            @ToolParam(description = "统计开始日期，格式yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "统计结束日期，格式yyyy-MM-dd", required = false) String endDate) {
        log.info("[Tool] get_cost_analysis 被调用, startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        String startOfMonth = today.withDayOfMonth(1).format(DATE_FORMATTER);
        String endOfMonth = today.format(DATE_FORMATTER);
        
        if (startDate != null && !startDate.isEmpty()) {
            startOfMonth = startDate;
        }
        if (endDate != null && !endDate.isEmpty()) {
            endOfMonth = endDate;
        }
        
        try {
            Result<CostStatisticsResponseDTO> costStats = costClient.getCostStatistics(startOfMonth, endOfMonth);
            if (costStats != null && costStats.getCode() == 200 && costStats.getData() != null) {
                CostStatisticsResponseDTO data = costStats.getData();
                result.put("monthlyCost", data.getTotalAmount());
                result.put("fuelCost", data.getFuelCost());
                result.put("maintenanceCost", data.getMaintenanceCost());
                result.put("laborCost", data.getLaborCost());
                result.put("insuranceCost", data.getInsuranceCost());
                result.put("depreciationCost", data.getDepreciationCost());
                result.put("managementCost", data.getManagementCost());
                result.put("otherCost", data.getOtherCost());
                result.put("tripCommissionCost", data.getTripCommissionCost());
            }

            Map<String, Object> tripStats = tripClient.getStatistics(startOfMonth, endOfMonth).getData();
            if (tripStats != null) {
                result.put("totalTrips", tripStats.getOrDefault("totalTrips", 0));
                result.put("totalDistance", tripStats.getOrDefault("totalDistance", 0.0));
                result.put("totalCargoWeight", tripStats.getOrDefault("totalCargoWeight", 0.0));
            }

            List<String> suggestions = new ArrayList<>();
            suggestions.add("定期检查车辆油耗，及时发现异常车辆");
            suggestions.add("优化调度路线，减少空驶里程");
            suggestions.add("合理安排车辆维护，避免紧急维修");
            suggestions.add("加强驾驶员培训，降低燃油消耗");
            result.put("suggestions", suggestions);
            
            result.put("status", "success");
            result.put("message", "获取成本分析成功");
            result.put("startDate", startOfMonth);
            result.put("endDate", endOfMonth);
            
        } catch (Exception e) {
            log.error("[Tool] get_cost_analysis 执行失败", e);
            result.put("status", "error");
            result.put("message", "获取成本分析失败: " + e.getMessage());
        }
        
        return result;
    }

    @Tool(name = "get_dispatch_optimization", description = "获取调度优化方案，包括今日任务数、进行中任务、已完成任务、可用车辆数等，以及调度优化建议")
    public Map<String, Object> getDispatchOptimization() {
        log.info("[Tool] get_dispatch_optimization 被调用");
        Map<String, Object> result = new HashMap<>();
        
        try {
            String today = LocalDate.now().format(DATE_FORMATTER);
            Map<String, Object> tripStats = tripClient.getStatistics(today, today).getData();
            
            if (tripStats != null) {
                result.put("todayTrips", tripStats.getOrDefault("totalTrips", 0));
                result.put("activeTrips", tripStats.getOrDefault("activeTrips", 0));
                result.put("completedTrips", tripStats.getOrDefault("completedTrips", 0));
            }

            Result<List<java.util.Map<String, Object>>> availableVehicles = vehicleClient.getAvailableVehicles();
            Integer availableCount = availableVehicles != null && availableVehicles.getCode() == 200 && availableVehicles.getData() != null ? availableVehicles.getData().size() : 0;
            result.put("availableVehicles", availableCount);

            List<String> suggestions = new ArrayList<>();
            suggestions.add("优先分配空闲车辆执行运输任务");
            suggestions.add("根据车辆当前位置智能派单，减少空驶时间");
            suggestions.add("合理安排驾驶员休息，保证运输效率");
            suggestions.add("实时监控任务进度，及时调整调度方案");
            result.put("suggestions", suggestions);
            
            result.put("status", "success");
            result.put("message", "获取调度优化方案成功");
            
        } catch (Exception e) {
            log.error("[Tool] get_dispatch_optimization 执行失败", e);
            result.put("status", "error");
            result.put("message", "获取调度优化方案失败: " + e.getMessage());
        }
        
        return result;
    }

    @Tool(name = "get_warning_statistics", description = "获取预警统计信息，包括今日预警数量、预警类型分布、7天趋势分析，以及安全建议")
    public Map<String, Object> getWarningStatistics(
            @ToolParam(description = "查询天数，默认7天", required = false) Integer days) {
        log.info("[Tool] get_warning_statistics 被调用, days={}", days);
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        String startTime = today.minusDays(days != null && days > 0 ? days : 7).atStartOfDay().toString().replace("T", " ");
        String endTime = today.plusDays(1).atStartOfDay().toString().replace("T", " ");
        
        try {
            Result<Map<String, Object>> warningStats = warningClient.getStatistics(startTime, endTime);
            if (warningStats != null && warningStats.getCode() == 200 && warningStats.getData() != null) {
                result.put("statistics", warningStats.getData());
            }

            Result<List<Map<String, Object>>> trendResult = warningClient.getTrend(days != null && days > 0 ? days : 7);
            if (trendResult != null && trendResult.getCode() == 200 && trendResult.getData() != null) {
                result.put("trend", trendResult.getData());
            }

            List<String> suggestions = new ArrayList<>();
            suggestions.add("加强超速预警关注，确保行车安全");
            suggestions.add("及时处理疲劳驾驶预警，降低事故风险");
            suggestions.add("定期分析预警数据，找出安全隐患");
            suggestions.add("加强驾驶员安全培训，提高安全意识");
            result.put("suggestions", suggestions);
            
            result.put("status", "success");
            result.put("message", "获取预警统计成功");
            
        } catch (Exception e) {
            log.error("[Tool] get_warning_statistics 执行失败", e);
            result.put("status", "error");
            result.put("message", "获取预警统计失败: " + e.getMessage());
        }
        
        return result;
    }

    @Tool(name = "get_trip_statistics", description = "获取行程统计信息，包括指定时间段的行程数量、里程、载货量等数据")
    public Map<String, Object> getTripStatistics(
            @ToolParam(description = "统计开始日期，格式yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "统计结束日期，格式yyyy-MM-dd", required = false) String endDate) {
        log.info("[Tool] get_trip_statistics 被调用, startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        String start = startDate != null && !startDate.isEmpty() ? startDate : today.format(DATE_FORMATTER);
        String end = endDate != null && !endDate.isEmpty() ? endDate : today.format(DATE_FORMATTER);
        
        try {
            Map<String, Object> tripStats = tripClient.getStatistics(start, end).getData();
            if (tripStats != null) {
                result.put("totalTrips", tripStats.getOrDefault("totalTrips", 0));
                result.put("activeTrips", tripStats.getOrDefault("activeTrips", 0));
                result.put("completedTrips", tripStats.getOrDefault("completedTrips", 0));
                result.put("totalDistance", tripStats.getOrDefault("totalDistance", 0.0));
                result.put("totalCargoWeight", tripStats.getOrDefault("totalCargoWeight", 0.0));
            }
            result.put("startDate", start);
            result.put("endDate", end);
            result.put("status", "success");
            
        } catch (Exception e) {
            log.error("[Tool] get_trip_statistics 执行失败", e);
            result.put("status", "error");
            result.put("message", "获取行程统计失败: " + e.getMessage());
        }
        
        return result;
    }

    @Tool(name = "search_vehicles", description = "搜索车辆信息，可以按车辆状态查找车辆列表")
    public Map<String, Object> searchVehicles(
            @ToolParam(description = "车辆状态：available-可用, fault-故障, maintenance-维修", required = false) String status) {
        log.info("[Tool] search_vehicles 被调用, status={}", status);
        Map<String, Object> result = new HashMap<>();
        
        try {
            if ("fault".equals(status)) {
                Result<List<java.util.Map<String, Object>>> vehicles = vehicleClient.getFaultVehicles();
                result.put("vehicles", vehicles != null ? vehicles.getData() : new ArrayList<>());
            } else if ("maintenance".equals(status)) {
                Result<List<java.util.Map<String, Object>>> vehicles = vehicleClient.getMaintenanceVehicles();
                result.put("vehicles", vehicles != null ? vehicles.getData() : new ArrayList<>());
            } else {
                Result<List<java.util.Map<String, Object>>> vehicles = vehicleClient.getAvailableVehicles();
                result.put("vehicles", vehicles != null ? vehicles.getData() : new ArrayList<>());
            }
            result.put("status", "success");
            result.put("searchStatus", status);
            
        } catch (Exception e) {
            log.error("[Tool] search_vehicles 执行失败", e);
            result.put("status", "error");
            result.put("message", "搜索车辆失败: " + e.getMessage());
        }
        
        return result;
    }
}
