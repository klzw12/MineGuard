package com.klzw.service.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.AiClient;
import com.klzw.common.core.client.CostClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.*;
import com.klzw.service.statistics.mapper.*;
import com.klzw.service.statistics.service.StatisticsService;
import com.klzw.service.statistics.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统计服务实现类（使用 TripClient/CostClient/VehicleClient/AiClient/WarningClient 调用各服务 + Redis 缓存）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TripStatisticsMapper tripStatisticsMapper;
    private final CostStatisticsMapper costStatisticsMapper;
    private final VehicleStatisticsMapper vehicleStatisticsMapper;
    private final DriverStatisticsMapper driverStatisticsMapper;
    private final TransportStatisticsMapper transportStatisticsMapper;
    private final FaultStatisticsMapper faultStatisticsMapper;
    
    private final TripClient tripClient;
    private final CostClient costClient;
    private final VehicleClient vehicleClient;
    private final UserClient userClient;
    private final AiClient aiClient;
    private final WarningClient warningClient;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final long CACHE_EXPIRE_DAYS = 1; // 缓存过期时间：1 天

    @Override
    public TripStatisticsVO calculateTripStatistics(String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:trip:" + date;
        redisTemplate.delete(cacheKey);
        
        TripStatistics existing = tripStatisticsMapper.selectOne(
            new LambdaQueryWrapper<TripStatistics>()
                .eq(TripStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            tripStatisticsMapper.deleteById(existing.getId());
        }
        
        TripStatistics entity = new TripStatistics();
        entity.setStatisticsDate(statisticsDate);
        
        try {
            var tripResult = tripClient.getStatistics(date, date);
            java.util.Map<String, Object> tripStats = tripResult != null && tripResult.getCode() == 200 ? tripResult.getData() : null;
            
            if (tripStats != null) {
                entity.setTripCount(tripStats.get("tripCount") != null ? ((Number) tripStats.get("tripCount")).intValue() : 0);
                entity.setTotalDistance(tripStats.get("totalDistance") != null ? new java.math.BigDecimal(tripStats.get("totalDistance").toString()) : BigDecimal.ZERO);
                entity.setTotalDuration(tripStats.get("totalDuration") != null ? new java.math.BigDecimal(tripStats.get("totalDuration").toString()) : BigDecimal.ZERO);
                entity.setCompletedTripCount(tripStats.get("completedTripCount") != null ? ((Number) tripStats.get("completedTripCount")).intValue() : 0);
                entity.setCancelledTripCount(tripStats.get("cancelledTripCount") != null ? ((Number) tripStats.get("cancelledTripCount")).intValue() : 0);
                entity.setAverageSpeed(tripStats.get("averageSpeed") != null ? new java.math.BigDecimal(tripStats.get("averageSpeed").toString()) : BigDecimal.ZERO);
                entity.setFuelConsumption(tripStats.get("fuelConsumption") != null ? new java.math.BigDecimal(tripStats.get("fuelConsumption").toString()) : BigDecimal.ZERO);
                entity.setCargoWeight(tripStats.get("cargoWeight") != null ? new java.math.BigDecimal(tripStats.get("cargoWeight").toString()) : BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.warn("从行程服务获取统计数据失败，使用默认值：{}", e.getMessage());
            entity.setTripCount(0);
            entity.setTotalDistance(BigDecimal.ZERO);
            entity.setTotalDuration(BigDecimal.ZERO);
            entity.setCompletedTripCount(0);
            entity.setCancelledTripCount(0);
            entity.setAverageSpeed(BigDecimal.ZERO);
            entity.setFuelConsumption(BigDecimal.ZERO);
            entity.setCargoWeight(BigDecimal.ZERO);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        tripStatisticsMapper.insert(entity);
        log.info("计算行程统计数据：日期={}, 行程数={}", date, entity.getTripCount());
        
        TripStatisticsVO vo = convertToTripStatisticsVO(entity);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public CostStatisticsVO calculateCostStatistics(String month) {
        LocalDate statisticsMonth = LocalDate.parse(month).withDayOfMonth(1);
        LocalDate monthStart = statisticsMonth;
        LocalDate monthEnd = statisticsMonth.plusMonths(1);
        
        String cacheKey = "statistics:cost:" + statisticsMonth.toString();
        redisTemplate.delete(cacheKey);
        
        CostStatistics existing = costStatisticsMapper.selectOne(
            new LambdaQueryWrapper<CostStatistics>()
                .eq(CostStatistics::getStatisticsMonth, statisticsMonth)
        );
        
        if (existing != null) {
            costStatisticsMapper.deleteById(existing.getId());
        }
        
        CostStatistics entity = new CostStatistics();
        entity.setStatisticsMonth(statisticsMonth);
        
        try {
            costClient.calculateSalaries(statisticsMonth.toString());
            log.info("已触发薪资计算：month={}", statisticsMonth);
        } catch (Exception e) {
            log.warn("触发薪资计算失败：{}", e.getMessage());
        }
        
        try {
            var costStatsResult = costClient.getCostStatistics(monthStart.toString(), monthEnd.toString());
            com.klzw.common.core.domain.dto.CostStatisticsResponseDTO costStats = costStatsResult != null && costStatsResult.isSuccess() ? costStatsResult.getData() : null;
            
            if (costStats != null) {
                entity.setFuelCost(costStats.getFuelCost() != null ? costStats.getFuelCost() : BigDecimal.ZERO);
                entity.setMaintenanceCost(costStats.getMaintenanceCost() != null ? costStats.getMaintenanceCost() : BigDecimal.ZERO);
                entity.setLaborCost(costStats.getLaborCost() != null ? costStats.getLaborCost() : BigDecimal.ZERO);
                entity.setInsuranceCost(costStats.getInsuranceCost() != null ? costStats.getInsuranceCost() : BigDecimal.ZERO);
                entity.setDepreciationCost(costStats.getDepreciationCost() != null ? costStats.getDepreciationCost() : BigDecimal.ZERO);
                entity.setManagementCost(costStats.getManagementCost() != null ? costStats.getManagementCost() : BigDecimal.ZERO);
                entity.setOtherCost(costStats.getOtherCost() != null ? costStats.getOtherCost() : BigDecimal.ZERO);
                
                BigDecimal totalCost = entity.getFuelCost()
                    .add(entity.getMaintenanceCost())
                    .add(entity.getLaborCost())
                    .add(entity.getInsuranceCost())
                    .add(entity.getDepreciationCost())
                    .add(entity.getManagementCost())
                    .add(entity.getOtherCost());
                entity.setTotalCost(totalCost);
            }
        } catch (Exception e) {
            log.warn("从成本服务获取统计数据失败，使用默认值：{}", e.getMessage());
            entity.setFuelCost(BigDecimal.ZERO);
            entity.setMaintenanceCost(BigDecimal.ZERO);
            entity.setLaborCost(BigDecimal.ZERO);
            entity.setInsuranceCost(BigDecimal.ZERO);
            entity.setDepreciationCost(BigDecimal.ZERO);
            entity.setManagementCost(BigDecimal.ZERO);
            entity.setOtherCost(BigDecimal.ZERO);
            entity.setTotalCost(BigDecimal.ZERO);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        costStatisticsMapper.insert(entity);
        log.info("计算成本统计数据：月份={} ({} ~ {}), 总成本={}", statisticsMonth, monthStart, monthEnd, entity.getTotalCost());
        
        CostStatisticsVO vo = convertToCostStatisticsVO(entity);
        
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public VehicleStatisticsVO calculateVehicleStatistics(Long vehicleId, String startDate, String endDate) {
        LocalDate statisticsDate = LocalDate.parse(endDate);
        
        String cacheKey = "statistics:vehicle:" + vehicleId + ":" + endDate;
        redisTemplate.delete(cacheKey);
        
        VehicleStatistics existing = vehicleStatisticsMapper.selectOne(
            new LambdaQueryWrapper<VehicleStatistics>()
                .eq(VehicleStatistics::getVehicleId, vehicleId)
                .eq(VehicleStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            vehicleStatisticsMapper.deleteById(existing.getId());
        }
        
        VehicleStatistics entity = new VehicleStatistics();
        entity.setVehicleId(vehicleId);
        entity.setStatisticsDate(statisticsDate);
        
        entity.setTripCount(0);
        entity.setTotalDistance(BigDecimal.ZERO);
        entity.setTotalDuration(BigDecimal.ZERO);
        entity.setCargoWeight(BigDecimal.ZERO);
        entity.setFuelConsumption(BigDecimal.ZERO);
        entity.setFuelCost(BigDecimal.ZERO);
        entity.setMaintenanceCount(0);
        entity.setMaintenanceCost(BigDecimal.ZERO);
        entity.setWarningCount(0);
        entity.setViolationCount(0);
        entity.setIdleDuration(BigDecimal.ZERO);
        entity.setIdleDistance(BigDecimal.ZERO);
        
        try {
            var tripResult = tripClient.getVehicleTripStatistics(vehicleId, startDate, endDate);
            log.info("车辆行程统计返回：vehicleId={}, result={}", vehicleId, tripResult);
            if (tripResult != null && tripResult.getCode() == 200 && tripResult.getData() != null) {
                Map<String, Object> tripStats = tripResult.getData();
                log.info("车辆行程统计数据：tripStats={}", tripStats);
                entity.setTripCount(getIntegerValue(tripStats, "tripCount"));
                entity.setTotalDistance(getBigDecimalValue(tripStats, "totalMileage"));
                entity.setTotalDuration(getBigDecimalValue(tripStats, "totalDuration"));
                entity.setCargoWeight(getBigDecimalValue(tripStats, "totalCargoWeight"));
                log.info("设置货物重量：cargoWeight={}", entity.getCargoWeight());
                entity.setFuelConsumption(getBigDecimalValue(tripStats, "totalFuelConsumption"));
                entity.setIdleDuration(getBigDecimalValue(tripStats, "totalIdleDuration"));
                entity.setIdleDistance(getBigDecimalValue(tripStats, "totalIdleDistance"));
            }
        } catch (Exception e) {
            log.warn("获取车辆行程统计失败：vehicleId={}, error={}", vehicleId, e.getMessage());
        }
        
        try {
            var warningResult = warningClient.getStatistics(startDate + "T00:00:00", endDate + "T23:59:59");
            if (warningResult != null && warningResult.getCode() == 200 && warningResult.getData() != null) {
                Map<String, Object> warningStats = warningResult.getData();
                entity.setWarningCount(getIntegerValue(warningStats, "totalCount"));
            }
        } catch (Exception e) {
            log.warn("获取车辆预警统计失败：vehicleId={}, error={}", vehicleId, e.getMessage());
        }
        
        try {
            var faultResult = vehicleClient.getFaultStatistics(vehicleId, endDate);
            if (faultResult != null && faultResult.getCode() == 200 && faultResult.getData() != null) {
                Map<String, Object> faultStats = faultResult.getData();
                entity.setMaintenanceCount(getIntegerValue(faultStats, "faultCount"));
                entity.setMaintenanceCost(getBigDecimalValue(faultStats, "totalRepairCost"));
            }
        } catch (Exception e) {
            log.warn("获取车辆故障统计失败：vehicleId={}, error={}", vehicleId, e.getMessage());
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        vehicleStatisticsMapper.insert(entity);
        log.info("计算车辆统计数据：车辆 ID={}, 统计日期={}, 行程数={}", vehicleId, statisticsDate, entity.getTripCount());
        
        VehicleStatisticsVO vo = convertToVehicleStatisticsVO(entity);
        
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public DriverStatisticsVO calculateDriverStatistics(Long userId, String startDate, String endDate) {
        LocalDate statisticsDate = LocalDate.parse(endDate);
        
        String cacheKey = "statistics:driver:" + userId + ":" + endDate;
        redisTemplate.delete(cacheKey);
        
        DriverStatistics existing = driverStatisticsMapper.selectOne(
            new LambdaQueryWrapper<DriverStatistics>()
                .eq(DriverStatistics::getUserId, userId)
                .eq(DriverStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            driverStatisticsMapper.deleteById(existing.getId());
        }
        
        DriverStatistics entity = new DriverStatistics();
        entity.setUserId(userId);
        entity.setStatisticsDate(statisticsDate);
        
        try {
            var driverResult = userClient.getDriverByUserId(userId);
            if (driverResult != null && driverResult.isSuccess() && driverResult.getData() != null) {
                Object driverData = driverResult.getData();
                if (driverData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> driverMap = (Map<String, Object>) driverData;
                    entity.setUserName((String) driverMap.get("driverName"));
                }
            }
        } catch (Exception e) {
            log.warn("获取司机信息失败：userId={}, error={}", userId, e.getMessage());
        }
        
        try {
            var tripResult = tripClient.getDriverStatistics(userId, startDate, endDate);
            log.info("获取司机行程统计返回：userId={}, result={}", userId, tripResult);
            if (tripResult != null && tripResult.getCode() == 200 && tripResult.getData() != null) {
                Map<String, Object> tripStats = tripResult.getData();
                entity.setTripCount(getIntegerValue(tripStats, "tripCount"));
                entity.setTotalDistance(getBigDecimalValue(tripStats, "totalDistance"));
                entity.setCargoWeight(getBigDecimalValue(tripStats, "cargoWeight"));
            }
        } catch (Exception e) {
            log.warn("获取司机行程统计失败：userId={}, error={}", userId, e.getMessage());
            entity.setTripCount(0);
            entity.setTotalDistance(BigDecimal.ZERO);
            entity.setCargoWeight(BigDecimal.ZERO);
        }
        
        try {
            var costResult = costClient.getDriverCostStatistics(userId, startDate, endDate);
            if (costResult != null && costResult.getCode() == 200 && costResult.getData() != null) {
                Map<String, Object> costStats = costResult.getData();
                entity.setFuelCost(getBigDecimalValue(costStats, "fuelCost"));
                entity.setTollCost(getBigDecimalValue(costStats, "tollCost"));
                entity.setCommissionAmount(getBigDecimalValue(costStats, "commissionAmount"));
                entity.setTotalCost(getBigDecimalValue(costStats, "totalCost"));
            }
        } catch (Exception e) {
            log.warn("获取司机成本统计失败：userId={}, error={}", userId, e.getMessage());
            entity.setFuelCost(BigDecimal.ZERO);
            entity.setTollCost(BigDecimal.ZERO);
            entity.setCommissionAmount(BigDecimal.ZERO);
            entity.setTotalCost(BigDecimal.ZERO);
        }
        
        try {
            var warningResult = warningClient.getStatistics(startDate + "T00:00:00", endDate + "T23:59:59");
            if (warningResult != null && warningResult.getCode() == 200 && warningResult.getData() != null) {
                Map<String, Object> warningStats = warningResult.getData();
                entity.setWarningCount(getIntegerValue(warningStats, "totalCount"));
            }
        } catch (Exception e) {
            log.warn("获取司机预警统计失败：userId={}, error={}", userId, e.getMessage());
            entity.setWarningCount(0);
        }
        
        int baseScore = 100;
        int warningDeduction = (entity.getWarningCount() != null ? entity.getWarningCount() : 0) * 5;
        entity.setPerformanceScore(BigDecimal.valueOf(Math.max(0, baseScore - warningDeduction)));
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            var attendanceResult = userClient.getAttendanceStatistics(userId, start, end);
            log.info("获取司机考勤统计返回：userId={}, result={}", userId, attendanceResult);
            if (attendanceResult != null && attendanceResult.getCode() == 200 && attendanceResult.getData() != null) {
                Map<String, Object> attendanceStats = attendanceResult.getData();
                log.info("司机考勤统计数据：attendanceStats={}", attendanceStats);
                entity.setAttendanceDays(getIntegerValue(attendanceStats, "actualAttendanceDays"));
                entity.setAttendanceHours(getBigDecimalValue(attendanceStats, "attendanceHours"));
                entity.setLateCount(getIntegerValue(attendanceStats, "lateTimes"));
                entity.setEarlyLeaveCount(getIntegerValue(attendanceStats, "earlyLeaveTimes"));
            }
        } catch (Exception e) {
            log.warn("获取司机考勤统计失败：userId={}, error={}", userId, e.getMessage());
        }
        
        entity.setViolationCount(0);
        entity.setOverSpeedCount(0);
        entity.setRouteDeviationCount(0);
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        driverStatisticsMapper.insert(entity);
        log.info("计算司机统计数据：用户 ID={}, 统计日期={}, 行程数={}", userId, statisticsDate, entity.getTripCount());
        
        DriverStatisticsVO vo = convertToDriverStatisticsVO(entity);
        
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public List<TripStatisticsVO> getTripStatistics(StatisticsQueryDTO queryDTO) {
        // 先尝试从缓存读取（如果是单个日期查询）
        if (queryDTO.getStartDate() != null && queryDTO.getStartDate().equals(queryDTO.getEndDate())) {
            String cacheKey = "statistics:trip:" + queryDTO.getStartDate();
            @SuppressWarnings("unchecked")
            TripStatisticsVO cached = (TripStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中：{}", cacheKey);
                return List.of(cached);
            }
        }
        
        LambdaQueryWrapper<TripStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(TripStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(TripStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(TripStatistics::getStatisticsDate);
        
        List<TripStatistics> list = tripStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToTripStatisticsVO).collect(Collectors.toList());
    }

    @Override
    public List<CostStatisticsVO> getCostStatistics(StatisticsQueryDTO queryDTO) {
        // 先尝试从缓存读取（如果是单个日期查询）
        if (queryDTO.getStartDate() != null && queryDTO.getStartDate().equals(queryDTO.getEndDate())) {
            String cacheKey = "statistics:cost:" + queryDTO.getStartDate();
            @SuppressWarnings("unchecked")
            CostStatisticsVO cached = (CostStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中：{}", cacheKey);
                return List.of(cached);
            }
        }
        
        LambdaQueryWrapper<CostStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(CostStatistics::getStatisticsMonth, queryDTO.getStartDate().withDayOfMonth(1));
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(CostStatistics::getStatisticsMonth, queryDTO.getEndDate().withDayOfMonth(1));
        }
        
        wrapper.orderByDesc(CostStatistics::getStatisticsMonth);
        
        List<CostStatistics> list = costStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToCostStatisticsVO).collect(Collectors.toList());
    }

    @Override
    public List<DriverStatisticsVO> getDriverStatistics(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<DriverStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getUserId() != null) {
            wrapper.eq(DriverStatistics::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(DriverStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(DriverStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(DriverStatistics::getStatisticsDate);
        
        List<DriverStatistics> list = driverStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToDriverStatisticsVO).collect(Collectors.toList());
    }

    @Override
    public OverallStatisticsVO getOverallStatistics(StatisticsQueryDTO queryDTO) {
        List<TripStatisticsVO> tripList = getTripStatistics(queryDTO);
        List<CostStatisticsVO> costList = getCostStatistics(queryDTO);
        
        int totalTripCount = 0;
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalDuration = BigDecimal.ZERO;
        int totalCompletedTripCount = 0;
        int totalCancelledTripCount = 0;
        BigDecimal totalCargoWeight = BigDecimal.ZERO;
        
        for (TripStatisticsVO trip : tripList) {
            if (trip.getTripCount() != null) totalTripCount += trip.getTripCount();
            if (trip.getTotalDistance() != null) totalDistance = totalDistance.add(trip.getTotalDistance());
            if (trip.getTotalDuration() != null) totalDuration = totalDuration.add(trip.getTotalDuration());
            if (trip.getCompletedTripCount() != null) totalCompletedTripCount += trip.getCompletedTripCount();
            if (trip.getCancelledTripCount() != null) totalCancelledTripCount += trip.getCancelledTripCount();
            if (trip.getCargoWeight() != null) totalCargoWeight = totalCargoWeight.add(trip.getCargoWeight());
        }
        
        BigDecimal totalFuelCost = BigDecimal.ZERO;
        BigDecimal totalMaintenanceCost = BigDecimal.ZERO;
        BigDecimal totalLaborCost = BigDecimal.ZERO;
        BigDecimal totalOtherCost = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (CostStatisticsVO cost : costList) {
            if (cost.getFuelCost() != null) totalFuelCost = totalFuelCost.add(cost.getFuelCost());
            if (cost.getMaintenanceCost() != null) totalMaintenanceCost = totalMaintenanceCost.add(cost.getMaintenanceCost());
            if (cost.getLaborCost() != null) totalLaborCost = totalLaborCost.add(cost.getLaborCost());
            if (cost.getOtherCost() != null) totalOtherCost = totalOtherCost.add(cost.getOtherCost());
            if (cost.getTotalCost() != null) totalCost = totalCost.add(cost.getTotalCost());
        }
        
        // 获取车辆和司机总数
        int totalVehicles = 0;
        int totalDrivers = 0;
        
        try {
            // 尝试从车辆服务获取车辆总数
            var vehicleResult = vehicleClient.getVehicleCount();
            if (vehicleResult != null && vehicleResult.getCode() == 200 && vehicleResult.getData() != null) {
                totalVehicles = vehicleResult.getData();
            }
        } catch (Exception e) {
            log.warn("获取车辆总数失败：{}", e.getMessage());
        }
        
        try {
            var driverIdsResult = userClient.getDriverIds();
            if (driverIdsResult != null && driverIdsResult.getCode() == 200 && driverIdsResult.getData() != null) {
                totalDrivers = driverIdsResult.getData().size();
            }
        } catch (Exception e) {
            log.warn("获取司机总数失败：{}", e.getMessage());
        }
        
        OverallStatisticsVO vo = new OverallStatisticsVO();
        vo.setStartDate(queryDTO.getStartDate());
        vo.setEndDate(queryDTO.getEndDate());
        vo.setTotalTripCount(totalTripCount);
        vo.setTotalDistance(totalDistance);
        vo.setTotalDuration(totalDuration);
        vo.setTotalCompletedTripCount(totalCompletedTripCount);
        vo.setTotalCancelledTripCount(totalCancelledTripCount);
        vo.setTotalCargoWeight(totalCargoWeight);
        vo.setTotalFuelCost(totalFuelCost);
        vo.setTotalMaintenanceCost(totalMaintenanceCost);
        vo.setTotalLaborCost(totalLaborCost);
        vo.setTotalOtherCost(totalOtherCost);
        vo.setTotalCost(totalCost);
        vo.setTotalVehicles(totalVehicles);
        vo.setTotalDrivers(totalDrivers);
        vo.setTripStatisticsList(tripList);
        vo.setCostStatisticsList(costList);
        
        return vo;
    }

    @Override
    public TransportStatisticsVO calculateTransportStatistics(String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:transport:" + date;
        redisTemplate.delete(cacheKey);
        
        TransportStatistics existing = transportStatisticsMapper.selectOne(
            new LambdaQueryWrapper<TransportStatistics>()
                .eq(TransportStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            transportStatisticsMapper.deleteById(existing.getId());
        }
        
        TransportStatistics entity = new TransportStatistics();
        entity.setStatisticsDate(statisticsDate);
        
        try {
            List<TripStatistics> tripStats = tripStatisticsMapper.selectList(
                new LambdaQueryWrapper<TripStatistics>()
                    .eq(TripStatistics::getStatisticsDate, statisticsDate)
            );
            
            BigDecimal totalCargoWeight = BigDecimal.ZERO;
            int totalTrips = 0;
            
            for (TripStatistics ts : tripStats) {
                if (ts.getCargoWeight() != null) {
                    totalCargoWeight = totalCargoWeight.add(ts.getCargoWeight());
                }
                if (ts.getTripCount() != null) {
                    totalTrips += ts.getTripCount();
                }
            }
            
            entity.setTotalCargoWeight(totalCargoWeight);
            entity.setTotalTrips(totalTrips);
            
            try {
                var vehicleResult = vehicleClient.getVehicleCount();
                if (vehicleResult != null && vehicleResult.getCode() == 200 && vehicleResult.getData() != null) {
                    entity.setTotalVehicles(vehicleResult.getData());
                }
            } catch (Exception ex) {
                log.warn("获取车辆总数失败：{}", ex.getMessage());
                entity.setTotalVehicles(0);
            }
            
            try {
                var driverIdsResult = userClient.getDriverIds();
                if (driverIdsResult != null && driverIdsResult.getCode() == 200 && driverIdsResult.getData() != null) {
                    entity.setTotalDrivers(driverIdsResult.getData().size());
                }
            } catch (Exception ex) {
                log.warn("获取司机总数失败：{}", ex.getMessage());
                entity.setTotalDrivers(0);
            }
            
            if (totalTrips > 0) {
                entity.setAvgCargoPerTrip(totalCargoWeight.divide(BigDecimal.valueOf(totalTrips), 2, RoundingMode.HALF_UP));
            } else {
                entity.setAvgCargoPerTrip(BigDecimal.ZERO);
            }
            
            if (entity.getTotalVehicles() != null && entity.getTotalVehicles() > 0) {
                entity.setAvgTripsPerVehicle(BigDecimal.valueOf(totalTrips).divide(BigDecimal.valueOf(entity.getTotalVehicles()), 2, RoundingMode.HALF_UP));
            } else {
                entity.setAvgTripsPerVehicle(BigDecimal.ZERO);
            }
            
        } catch (Exception e) {
            log.warn("计算运输统计数据失败，使用默认值：{}", e.getMessage());
            entity.setTotalCargoWeight(BigDecimal.ZERO);
            entity.setTotalTrips(0);
            entity.setTotalVehicles(0);
            entity.setTotalDrivers(0);
            entity.setAvgCargoPerTrip(BigDecimal.ZERO);
            entity.setAvgTripsPerVehicle(BigDecimal.ZERO);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        transportStatisticsMapper.insert(entity);
        log.info("计算运输统计数据：日期={}, 行程数={}", date, entity.getTotalTrips());
        
        TransportStatisticsVO vo = convertToTransportStatisticsVO(entity);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public List<TransportStatisticsVO> getTransportStatistics(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<TransportStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(TransportStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(TransportStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(TransportStatistics::getStatisticsDate);
        
        List<TransportStatistics> list = transportStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToTransportStatisticsVO).collect(Collectors.toList());
    }
    
    @Override
    public FaultStatisticsVO getFaultOverallStatistics(StatisticsQueryDTO queryDTO) {
        LocalDate startDate = queryDTO.getStartDate() != null ? queryDTO.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = queryDTO.getEndDate() != null ? queryDTO.getEndDate() : LocalDate.now();
        
        LambdaQueryWrapper<FaultStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FaultStatistics::getStatisticsDate, startDate)
               .le(FaultStatistics::getStatisticsDate, endDate);
        
        List<FaultStatistics> list = faultStatisticsMapper.selectList(wrapper);
        
        if (list == null || list.isEmpty()) {
            return new FaultStatisticsVO();
        }
        
        int totalFaultCount = 0;
        int totalMinorFault = 0;
        int totalMajorFault = 0;
        int totalCriticalFault = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        int totalRepaired = 0;
        int totalPending = 0;
        
        for (FaultStatistics stat : list) {
            if (stat.getFaultCount() != null) totalFaultCount += stat.getFaultCount();
            if (stat.getMinorFaultCount() != null) totalMinorFault += stat.getMinorFaultCount();
            if (stat.getMajorFaultCount() != null) totalMajorFault += stat.getMajorFaultCount();
            if (stat.getCriticalFaultCount() != null) totalCriticalFault += stat.getCriticalFaultCount();
            if (stat.getTotalRepairCost() != null) totalCost = totalCost.add(stat.getTotalRepairCost());
            if (stat.getRepairedCount() != null) totalRepaired += stat.getRepairedCount();
            if (stat.getPendingCount() != null) totalPending += stat.getPendingCount();
        }
        
        FaultStatisticsVO vo = new FaultStatisticsVO();
        vo.setTotalFaultCount(totalFaultCount);
        vo.setFaultCount(totalFaultCount);
        vo.setMinorFaultCount(totalMinorFault);
        vo.setMajorFaultCount(totalMajorFault);
        vo.setCriticalFaultCount(totalCriticalFault);
        vo.setTotalCost(totalCost);
        vo.setTotalRepairCost(totalCost);
        vo.setRepairedCount(totalRepaired);
        vo.setPendingCount(totalPending);
        
        return vo;
    }

    @Override
    public void calculateFaultStatistics(Long vehicleId, String startDate, String endDate) {
        LocalDate statisticsDate = LocalDate.parse(endDate);
        
        String cacheKey = "statistics:fault:" + vehicleId + ":" + endDate;
        redisTemplate.delete(cacheKey);
        
        FaultStatistics existing = faultStatisticsMapper.selectOne(
            new LambdaQueryWrapper<FaultStatistics>()
                .eq(FaultStatistics::getVehicleId, vehicleId)
                .eq(FaultStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            faultStatisticsMapper.deleteById(existing.getId());
        }
        
        FaultStatistics entity = new FaultStatistics();
        entity.setVehicleId(vehicleId);
        entity.setStatisticsDate(statisticsDate);
        
        try {
            com.klzw.common.core.result.Result<java.util.Map<String, Object>> faultResult = vehicleClient.getFaultStatistics(vehicleId, endDate);
            java.util.Map<String, Object> faultStats = faultResult != null && faultResult.getCode() == 200 ? faultResult.getData() : null;
            
            if (faultStats != null) {
                entity.setFaultCount(getIntegerValue(faultStats, "faultCount"));
                entity.setMinorFaultCount(getIntegerValue(faultStats, "minorFaultCount"));
                entity.setMajorFaultCount(getIntegerValue(faultStats, "majorFaultCount"));
                entity.setCriticalFaultCount(getIntegerValue(faultStats, "criticalFaultCount"));
                entity.setTotalRepairCost(getBigDecimalValue(faultStats, "totalRepairCost"));
                entity.setAvgRepairTime(getBigDecimalValue(faultStats, "avgRepairTime"));
                entity.setTopFaultType((String) faultStats.get("topFaultType"));
                entity.setTopFaultCount(getIntegerValue(faultStats, "topFaultCount"));
                entity.setRepairedCount(getIntegerValue(faultStats, "repairedCount"));
                entity.setPendingCount(getIntegerValue(faultStats, "pendingCount"));
            }
        } catch (Exception e) {
            log.warn("从车辆服务获取故障统计数据失败，使用默认值：{}", e.getMessage());
            entity.setFaultCount(0);
            entity.setMinorFaultCount(0);
            entity.setMajorFaultCount(0);
            entity.setCriticalFaultCount(0);
            entity.setTotalRepairCost(BigDecimal.ZERO);
            entity.setAvgRepairTime(BigDecimal.ZERO);
            entity.setTopFaultType("无");
            entity.setTopFaultCount(0);
            entity.setRepairedCount(0);
            entity.setPendingCount(0);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        faultStatisticsMapper.insert(entity);
        log.info("计算故障统计数据：车辆 ID={}, 统计日期={}, 故障数={}", vehicleId, statisticsDate, entity.getFaultCount());
        
        FaultStatisticsVO vo = convertToFaultStatisticsVO(entity);
        
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }
    
    @Override
    public FaultStatisticsVO getFaultStatistics(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<FaultStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getVehicleId() != null) {
            wrapper.eq(FaultStatistics::getVehicleId, queryDTO.getVehicleId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(FaultStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(FaultStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(FaultStatistics::getStatisticsDate);
        
        List<FaultStatistics> list = faultStatisticsMapper.selectList(wrapper);
        if (list != null && !list.isEmpty()) {
            return convertToFaultStatisticsVO(list.get(0));
        }
        return new FaultStatisticsVO();
    }

    private TripStatisticsVO convertToTripStatisticsVO(TripStatistics entity) {
        TripStatisticsVO vo = new TripStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private CostStatisticsVO convertToCostStatisticsVO(CostStatistics entity) {
        CostStatisticsVO vo = new CostStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private VehicleStatisticsVO convertToVehicleStatisticsVO(VehicleStatistics entity) {
        VehicleStatisticsVO vo = new VehicleStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        
        if (entity.getTotalDuration() != null && entity.getTotalDuration().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = entity.getTotalDistance()
                    .divide(entity.getTotalDuration(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            vo.setUtilizationRate(utilizationRate);
        } else {
            vo.setUtilizationRate(BigDecimal.ZERO);
        }
        
        return vo;
    }

    private DriverStatisticsVO convertToDriverStatisticsVO(DriverStatistics entity) {
        DriverStatisticsVO vo = new DriverStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private TransportStatisticsVO convertToTransportStatisticsVO(TransportStatistics entity) {
        TransportStatisticsVO vo = new TransportStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
    
    private FaultStatisticsVO convertToFaultStatisticsVO(FaultStatistics entity) {
        FaultStatisticsVO vo = new FaultStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
    
    @Override
    public void calculateMonthlyTripStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("计算月度行程统计：startDate={}, endDate={}", startDate, endDate);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            try {
                String dateStr = currentDate.toString();
                calculateTripStatistics(dateStr);
                log.debug("计算行程统计：日期={}", dateStr);
            } catch (Exception e) {
                log.warn("计算行程统计失败：日期={}, 错误={}", currentDate, e.getMessage());
            }
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("月度行程统计计算完成：共计算 {} 天", startDate.until(endDate).getDays() + 1);
    }
    
    @Override
    public void calculateMonthlyCostStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("计算月度成本统计：startDate={}, endDate={}", startDate, endDate);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            try {
                String dateStr = currentDate.toString();
                calculateCostStatistics(dateStr);
                log.debug("计算成本统计：日期={}", dateStr);
            } catch (Exception e) {
                log.warn("计算成本统计失败：日期={}, 错误={}", currentDate, e.getMessage());
            }
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("月度成本统计计算完成：共计算 {} 天", startDate.until(endDate).getDays() + 1);
    }
    
    /**
     * 从 Map 中获取 BigDecimal 值
     */
    private BigDecimal getBigDecimalValue(java.util.Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return new BigDecimal(obj.toString());
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            log.warn("转换 BigDecimal 失败：key={}, value={}", key, obj);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getIntegerValue(java.util.Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            log.warn("转换 Integer 失败：key={}, value={}", key, obj);
            return 0;
        }
    }

    public Map<String, Object> analyzeStatisticsWithAI(String date) {
        log.info("使用 AI 分析统计数据：date={}", date);
        try {
            Map<String, Object> statisticsData = new HashMap<>();
            statisticsData.put("date", date);
            statisticsData.put("tripStats", calculateTripStatistics(date));
            statisticsData.put("costStats", calculateCostStatistics(date));
            
            var result = aiClient.analyzeStatisticsData(statisticsData);
            if (result != null && result.getCode() == 200) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("AI 分析统计数据失败：date={}, error={}", date, e.getMessage());
        }
        return new HashMap<>();
    }

    public Map<String, Object> getWarningTrend(int days) {
        log.info("获取预警趋势：days={}", days);
        try {
            var result = warningClient.getTrend(days);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return Map.of("trend", result.getData());
            }
        } catch (Exception e) {
            log.warn("获取预警趋势失败：days={}, error={}", days, e.getMessage());
        }
        return new HashMap<>();
    }

    public Map<String, Object> getWarningStatistics(String startTime, String endTime) {
        log.info("获取预警统计：startTime={}, endTime={}", startTime, endTime);
        try {
            var result = warningClient.getStatistics(startTime, endTime);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取预警统计失败：startTime={}, endTime={}, error={}", startTime, endTime, e.getMessage());
        }
        return new HashMap<>();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<VehicleTripStatsVO> getVehicleTripStats(String dimension, String startDate, String endDate) {
        log.info("获取车辆出车统计：dimension={}, startDate={}, endDate={}", dimension, startDate, endDate);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        String cacheKey = "statistics:vehicle:trip:" + dimension + ":" + start + ":" + end;
        
        List<VehicleTripStatsVO> cached = (List<VehicleTripStatsVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取车辆出车统计：{} 条记录", cached.size());
            return cached;
        }
        
        List<VehicleTripStatsVO> result = new java.util.ArrayList<>();
        
        List<VehicleStatistics> statsList = vehicleStatisticsMapper.selectList(
            new LambdaQueryWrapper<VehicleStatistics>()
                .ge(VehicleStatistics::getStatisticsDate, start)
                .le(VehicleStatistics::getStatisticsDate, end)
        );
        
        if (statsList == null || statsList.isEmpty()) {
            log.info("未找到车辆统计数据：startDate={}, endDate={}", start, end);
            return result;
        }
        
        Map<Long, VehicleTripStatsVO> vehicleStatsMap = new java.util.HashMap<>();
        
        for (VehicleStatistics stats : statsList) {
            Long vehicleId = stats.getVehicleId();
            VehicleTripStatsVO vo = vehicleStatsMap.get(vehicleId);
            
            if (vo == null) {
                vo = new VehicleTripStatsVO();
                vo.setVehicleId(vehicleId);
                vo.setTripCount(0);
                vo.setTotalDistance(BigDecimal.ZERO);
                vo.setCargoWeight(BigDecimal.ZERO);
                vehicleStatsMap.put(vehicleId, vo);
                
                try {
                    var vehicleResult = vehicleClient.getById(vehicleId);
                    if (vehicleResult != null && vehicleResult.getCode() == 200 && vehicleResult.getData() != null) {
                        var vehicleData = vehicleResult.getData();
                        vo.setVehicleNo((String) vehicleData.get("vehicleNo"));
                    }
                } catch (Exception e) {
                    log.warn("获取车辆信息失败：vehicleId={}", vehicleId);
                    vo.setVehicleNo("未知车辆");
                }
            }
            
            vo.setTripCount(vo.getTripCount() + (stats.getTripCount() != null ? stats.getTripCount() : 0));
            vo.setTotalDistance(vo.getTotalDistance().add(stats.getTotalDistance() != null ? stats.getTotalDistance() : BigDecimal.ZERO));
            vo.setCargoWeight(vo.getCargoWeight().add(stats.getCargoWeight() != null ? stats.getCargoWeight() : BigDecimal.ZERO));
        }
        
        for (VehicleTripStatsVO vo : vehicleStatsMap.values()) {
            vo.setPeriod(start + " ~ " + end);
            result.add(vo);
        }
        
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("返回车辆出车统计：{} 条记录，已缓存", result.size());
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<DriverStatisticsVO> getDriverStatsAggregated(String dimension, String startDate, String endDate) {
        log.info("获取司机统计聚合：dimension={}, startDate={}, endDate={}", dimension, startDate, endDate);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        String cacheKey = "statistics:driver:aggregated:" + dimension + ":" + start + ":" + end;
        
        List<DriverStatisticsVO> cached = (List<DriverStatisticsVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取司机统计聚合：{} 条记录", cached.size());
            return cached;
        }
        
        List<DriverStatisticsVO> result = new java.util.ArrayList<>();
        
        List<DriverStatistics> statsList = driverStatisticsMapper.selectList(
            new LambdaQueryWrapper<DriverStatistics>()
                .ge(DriverStatistics::getStatisticsDate, start)
                .le(DriverStatistics::getStatisticsDate, end)
        );
        
        if (statsList == null || statsList.isEmpty()) {
            log.info("未找到司机统计数据：startDate={}, endDate={}", start, end);
            return result;
        }
        
        Map<Long, DriverStatisticsVO> driverStatsMap = new java.util.HashMap<>();
        
        for (DriverStatistics stats : statsList) {
            Long userId = stats.getUserId();
            DriverStatisticsVO vo = driverStatsMap.get(userId);
            
            if (vo == null) {
                vo = new DriverStatisticsVO();
                vo.setUserId(userId);
                vo.setUserName(stats.getUserName());
                vo.setAttendanceDays(0);
                vo.setAttendanceHours(BigDecimal.ZERO);
                vo.setTripCount(0);
                vo.setTotalDistance(BigDecimal.ZERO);
                vo.setCargoWeight(BigDecimal.ZERO);
                vo.setLateCount(0);
                vo.setEarlyLeaveCount(0);
                vo.setWarningCount(0);
                vo.setPerformanceScore(BigDecimal.ZERO);
                vo.setFuelCost(BigDecimal.ZERO);
                vo.setTollCost(BigDecimal.ZERO);
                vo.setCommissionAmount(BigDecimal.ZERO);
                vo.setTotalCost(BigDecimal.ZERO);
                driverStatsMap.put(userId, vo);
            }
            
            vo.setAttendanceDays(vo.getAttendanceDays() + (stats.getAttendanceDays() != null ? stats.getAttendanceDays() : 0));
            vo.setAttendanceHours(vo.getAttendanceHours().add(stats.getAttendanceHours() != null ? stats.getAttendanceHours() : BigDecimal.ZERO));
            vo.setTripCount(vo.getTripCount() + (stats.getTripCount() != null ? stats.getTripCount() : 0));
            vo.setTotalDistance(vo.getTotalDistance().add(stats.getTotalDistance() != null ? stats.getTotalDistance() : BigDecimal.ZERO));
            vo.setCargoWeight(vo.getCargoWeight().add(stats.getCargoWeight() != null ? stats.getCargoWeight() : BigDecimal.ZERO));
            vo.setLateCount(vo.getLateCount() + (stats.getLateCount() != null ? stats.getLateCount() : 0));
            vo.setEarlyLeaveCount(vo.getEarlyLeaveCount() + (stats.getEarlyLeaveCount() != null ? stats.getEarlyLeaveCount() : 0));
            vo.setWarningCount(vo.getWarningCount() + (stats.getWarningCount() != null ? stats.getWarningCount() : 0));
            vo.setFuelCost(vo.getFuelCost().add(stats.getFuelCost() != null ? stats.getFuelCost() : BigDecimal.ZERO));
            vo.setTollCost(vo.getTollCost().add(stats.getTollCost() != null ? stats.getTollCost() : BigDecimal.ZERO));
            vo.setCommissionAmount(vo.getCommissionAmount().add(stats.getCommissionAmount() != null ? stats.getCommissionAmount() : BigDecimal.ZERO));
            vo.setTotalCost(vo.getTotalCost().add(stats.getTotalCost() != null ? stats.getTotalCost() : BigDecimal.ZERO));
            
            if (stats.getPerformanceScore() != null) {
                vo.setPerformanceScore(vo.getPerformanceScore().add(stats.getPerformanceScore()));
            }
        }
        
        int recordCount = statsList.size();
        for (DriverStatisticsVO vo : driverStatsMap.values()) {
            vo.setPeriod(start + " ~ " + end);
            if (recordCount > 0 && vo.getPerformanceScore().compareTo(BigDecimal.ZERO) > 0) {
                vo.setPerformanceScore(vo.getPerformanceScore().divide(BigDecimal.valueOf(recordCount), 0, java.math.RoundingMode.HALF_UP));
            }
            result.add(vo);
        }
        
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("返回司机统计聚合：{} 条记录，已缓存", result.size());
        return result;
    }
    
    private void calculateVehicleTripData(VehicleTripStatsVO vo, LocalDate start, LocalDate end) {
        try {
            var tripResult = tripClient.getVehicleTripStatistics(vo.getVehicleId(), start.toString(), end.toString());
            if (tripResult != null && tripResult.getCode() == 200 && tripResult.getData() != null) {
                var data = tripResult.getData();
                vo.setTripCount(getIntegerValue(data, "tripCount"));
                vo.setTotalDistance(getBigDecimalValue(data, "totalMileage"));
            }
        } catch (Exception e) {
            log.warn("获取车辆行程统计失败：vehicleId={}", vo.getVehicleId());
        }
        
        try {
            var vehicleResult = vehicleClient.getById(vo.getVehicleId());
            if (vehicleResult != null && vehicleResult.getCode() == 200 && vehicleResult.getData() != null) {
                var vehicleData = vehicleResult.getData();
                Object cargoWeightObj = vehicleData.get("cargoWeight");
                if (cargoWeightObj != null) {
                    if (cargoWeightObj instanceof BigDecimal) {
                        vo.setCargoWeight((BigDecimal) cargoWeightObj);
                    } else if (cargoWeightObj instanceof Number) {
                        vo.setCargoWeight(BigDecimal.valueOf(((Number) cargoWeightObj).doubleValue()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取车辆货运量失败：vehicleId={}", vo.getVehicleId());
        }
        
        if (vo.getTripCount() == null) vo.setTripCount(0);
        if (vo.getTotalDistance() == null) vo.setTotalDistance(BigDecimal.ZERO);
        if (vo.getCargoWeight() == null) vo.setCargoWeight(BigDecimal.ZERO);
    }
    
    @Override
    public void calculateAllDriverStatistics(String startDate, String endDate) {
        log.info("批量计算所有司机统计：startDate={}, endDate={}", startDate, endDate);
        try {
            var result = userClient.getDriverIds();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<Long> driverIds = result.getData();
                int successCount = 0;
                int failCount = 0;
                for (Long driverId : driverIds) {
                    try {
                        calculateDriverStatistics(driverId, startDate, endDate);
                        successCount++;
                    } catch (Exception e) {
                        log.warn("计算司机统计失败：driverId={}, error={}", driverId, e.getMessage());
                        failCount++;
                    }
                }
                log.info("批量计算司机统计完成：成功={}, 失败={}", successCount, failCount);
                
                var keys = redisTemplate.keys("statistics:driver:aggregated:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("清除司机统计聚合缓存：{} 个", keys.size());
                }
            }
        } catch (Exception e) {
            log.error("批量计算司机统计失败", e);
        }
    }
    
    @Override
    public void calculateAllFaultStatistics(String startDate, String endDate) {
        log.info("批量计算所有故障统计：startDate={}, endDate={}", startDate, endDate);
        try {
            var result = vehicleClient.getVehicleCount();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                Integer totalVehicles = result.getData();
                log.info("开始计算 {} 辆车的故障统计", totalVehicles);
                
                var vehiclesResult = vehicleClient.getIdleVehicles();
                if (vehiclesResult != null && vehiclesResult.getCode() == 200 && vehiclesResult.getData() != null) {
                    var vehicles = vehiclesResult.getData();
                    int successCount = 0;
                    int failCount = 0;
                    for (var vehicleMap : vehicles) {
                        try {
                            Object idObj = vehicleMap.get("id");
                            Long vehicleId = null;
                            if (idObj instanceof Number) {
                                vehicleId = ((Number) idObj).longValue();
                            } else if (idObj instanceof String) {
                                vehicleId = Long.parseLong((String) idObj);
                            }
                            if (vehicleId != null) {
                                calculateFaultStatistics(vehicleId, startDate, endDate);
                                successCount++;
                            }
                        } catch (Exception e) {
                            log.warn("计算故障统计失败：error={}", e.getMessage());
                            failCount++;
                        }
                    }
                    log.info("批量计算故障统计完成：成功={}, 失败={}", successCount, failCount);
                }
            }
        } catch (Exception e) {
            log.error("批量计算故障统计失败", e);
        }
    }
    
    @Override
    public void calculateAllVehicleStatistics(String startDate, String endDate) {
        log.info("批量计算所有车辆统计：startDate={}, endDate={}", startDate, endDate);
        try {
            var vehiclesResult = vehicleClient.getVehicleIds();
            if (vehiclesResult != null && vehiclesResult.getCode() == 200 && vehiclesResult.getData() != null) {
                List<Long> vehicleIds = vehiclesResult.getData();
                
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                List<LocalDate> dates = new java.util.ArrayList<>();
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    dates.add(date);
                }
                
                int successCount = 0;
                int failCount = 0;
                
                for (LocalDate date : dates) {
                    String dateStr = date.toString();
                    for (Long vehicleId : vehicleIds) {
                        try {
                            calculateVehicleStatistics(vehicleId, dateStr, dateStr);
                            successCount++;
                        } catch (Exception e) {
                            log.warn("计算车辆统计失败：vehicleId={}, date={}, error={}", vehicleId, dateStr, e.getMessage());
                            failCount++;
                        }
                    }
                }
                
                log.info("批量计算车辆统计完成：成功={}, 失败={}", successCount, failCount);
                
                var keys = redisTemplate.keys("statistics:vehicle:trip:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("清除车辆出车统计缓存：{} 个", keys.size());
                }
            }
        } catch (Exception e) {
            log.error("批量计算车辆统计失败", e);
        }
    }
    
    @Override
    public void backfillStatistics(String date) {
        log.info("开始补录统计数据：date={}", date);
        
        LocalDate statisticsDate = LocalDate.parse(date);
        LocalDate previousDate = statisticsDate.minusDays(1);
        String previousDateStr = previousDate.toString();
        
        int successCount = 0;
        int failCount = 0;
        
        try {
            calculateTripStatistics(date);
            log.info("补录行程统计成功：date={}", date);
            successCount++;
        } catch (Exception e) {
            log.warn("补录行程统计失败：date={}, error={}", date, e.getMessage());
            failCount++;
        }
        
        try {
            calculateCostStatistics(date);
            log.info("补录成本统计成功：date={}", date);
            successCount++;
        } catch (Exception e) {
            log.warn("补录成本统计失败：date={}, error={}", date, e.getMessage());
            failCount++;
        }
        
        try {
            calculateTransportStatistics(date);
            log.info("补录运输统计成功：date={}", date);
            successCount++;
        } catch (Exception e) {
            log.warn("补录运输统计失败：date={}, error={}", date, e.getMessage());
            failCount++;
        }
        
        try {
            var vehiclesResult = vehicleClient.getIdleVehicles();
            if (vehiclesResult != null && vehiclesResult.getCode() == 200 && vehiclesResult.getData() != null) {
                var vehicles = vehiclesResult.getData();
                int vehicleSuccess = 0;
                int vehicleFail = 0;
                for (var vehicleMap : vehicles) {
                    try {
                        Object idObj = vehicleMap.get("id");
                        Long vehicleId = null;
                        if (idObj instanceof Number) {
                            vehicleId = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            vehicleId = Long.parseLong((String) idObj);
                        }
                        if (vehicleId != null) {
                            calculateVehicleStatistics(vehicleId, date, date);
                            vehicleSuccess++;
                        }
                    } catch (Exception e) {
                        log.warn("补录车辆统计失败：vehicleId={}, error={}", vehicleMap.get("id"), e.getMessage());
                        vehicleFail++;
                    }
                }
                log.info("补录车辆统计完成：成功={}, 失败={}", vehicleSuccess, vehicleFail);
            }
        } catch (Exception e) {
            log.warn("补录车辆统计失败：error={}", e.getMessage());
        }
        
        try {
            var result = userClient.getDriverIds();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<Long> driverIds = result.getData();
                int driverSuccess = 0;
                int driverFail = 0;
                for (Long driverId : driverIds) {
                    try {
                        calculateDriverStatistics(driverId, date, date);
                        driverSuccess++;
                    } catch (Exception e) {
                        log.warn("补录司机统计失败：driverId={}, error={}", driverId, e.getMessage());
                        driverFail++;
                    }
                }
                log.info("补录司机统计完成：成功={}, 失败={}", driverSuccess, driverFail);
            }
        } catch (Exception e) {
            log.warn("补录司机统计失败：error={}", e.getMessage());
        }
        
        try {
            var vehiclesResult = vehicleClient.getIdleVehicles();
            if (vehiclesResult != null && vehiclesResult.getCode() == 200 && vehiclesResult.getData() != null) {
                var vehicles = vehiclesResult.getData();
                int faultSuccess = 0;
                int faultFail = 0;
                for (var vehicleMap : vehicles) {
                    try {
                        Object idObj = vehicleMap.get("id");
                        Long vehicleId = null;
                        if (idObj instanceof Number) {
                            vehicleId = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            vehicleId = Long.parseLong((String) idObj);
                        }
                        if (vehicleId != null) {
                            calculateFaultStatistics(vehicleId, date, date);
                            faultSuccess++;
                        }
                    } catch (Exception e) {
                        log.warn("补录故障统计失败：vehicleId={}, error={}", vehicleMap.get("id"), e.getMessage());
                        faultFail++;
                    }
                }
                log.info("补录故障统计完成：成功={}, 失败={}", faultSuccess, faultFail);
            }
        } catch (Exception e) {
            log.warn("补录故障统计失败：error={}", e.getMessage());
        }
        
        log.info("补录统计数据完成：date={}, 成功={}, 失败={}", date, successCount, failCount);
    }
}
