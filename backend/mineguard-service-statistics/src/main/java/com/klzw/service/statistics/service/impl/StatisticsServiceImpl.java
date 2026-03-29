package com.klzw.service.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.StatisticsClient;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统计服务实现类（使用 StatisticsClient 调用其他服务 + Redis 缓存）
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
    private final StatisticsClient statisticsClient;
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
            // 使用 StatisticsClient 调用 trip-service
            java.util.Map<String, Object> tripStats = statisticsClient.getTripStatistics(date, date);
            
            if (tripStats != null) {
                Object tripCountObj = tripStats.get("tripCount");
                Object totalDistanceObj = tripStats.get("totalDistance");
                Object totalDurationObj = tripStats.get("totalDuration");
                Object completedTripCountObj = tripStats.get("completedTripCount");
                Object cancelledTripCountObj = tripStats.get("cancelledTripCount");
                Object averageSpeedObj = tripStats.get("averageSpeed");
                Object fuelConsumptionObj = tripStats.get("fuelConsumption");
                Object cargoWeightObj = tripStats.get("cargoWeight");
                
                entity.setTripCount(tripCountObj != null ? ((Number) tripCountObj).intValue() : 0);
                entity.setTotalDistance(totalDistanceObj != null ? new BigDecimal(totalDistanceObj.toString()) : BigDecimal.ZERO);
                entity.setTotalDuration(totalDurationObj != null ? new BigDecimal(totalDurationObj.toString()) : BigDecimal.ZERO);
                entity.setCompletedTripCount(completedTripCountObj != null ? ((Number) completedTripCountObj).intValue() : 0);
                entity.setCancelledTripCount(cancelledTripCountObj != null ? ((Number) cancelledTripCountObj).intValue() : 0);
                entity.setAverageSpeed(averageSpeedObj != null ? new BigDecimal(averageSpeedObj.toString()) : BigDecimal.ZERO);
                entity.setFuelConsumption(fuelConsumptionObj != null ? new BigDecimal(fuelConsumptionObj.toString()) : BigDecimal.ZERO);
                entity.setCargoWeight(cargoWeightObj != null ? new BigDecimal(cargoWeightObj.toString()) : BigDecimal.ZERO);
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
    public CostStatisticsVO calculateCostStatistics(String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:cost:" + date;
        redisTemplate.delete(cacheKey);
        
        CostStatistics existing = costStatisticsMapper.selectOne(
            new LambdaQueryWrapper<CostStatistics>()
                .eq(CostStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            costStatisticsMapper.deleteById(existing.getId());
        }
        
        CostStatistics entity = new CostStatistics();
        entity.setStatisticsDate(statisticsDate);
        
        try {
            // 调用 cost-service 获取成本统计数据
            java.util.Map<String, Object> costStats = statisticsClient.getCostStatistics(date, date);
            
            if (costStats != null) {
                entity.setFuelCost(getBigDecimalValue(costStats, "fuelCost"));
                entity.setMaintenanceCost(getBigDecimalValue(costStats, "maintenanceCost"));
                entity.setLaborCost(getBigDecimalValue(costStats, "laborCost"));
                entity.setInsuranceCost(getBigDecimalValue(costStats, "insuranceCost"));
                entity.setDepreciationCost(getBigDecimalValue(costStats, "depreciationCost"));
                entity.setManagementCost(getBigDecimalValue(costStats, "managementCost"));
                entity.setOtherCost(getBigDecimalValue(costStats, "otherCost"));
                
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
        log.info("计算成本统计数据：日期={}, 总成本={}", date, entity.getTotalCost());
        
        CostStatisticsVO vo = convertToCostStatisticsVO(entity);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public VehicleStatisticsVO calculateVehicleStatistics(Long vehicleId, String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:vehicle:" + vehicleId + ":" + date;
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
        
        try {
            // 使用 StatisticsClient 调用相关服务获取车辆统计数据
            // 这里可以调用多个服务获取不同维度的车辆数据
            // 例如：行程数据、故障数据、维护数据等
            // 为简化实现，暂时使用默认值
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
        } catch (Exception e) {
            log.warn("获取车辆统计数据失败，使用默认值：{}", e.getMessage());
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
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        vehicleStatisticsMapper.insert(entity);
        log.info("计算车辆统计数据：车辆 ID={}, 日期={}, 行程数={}", vehicleId, date, entity.getTripCount());
        
        VehicleStatisticsVO vo = convertToVehicleStatisticsVO(entity);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        
        return vo;
    }

    @Override
    public DriverStatisticsVO calculateDriverStatistics(Long userId, String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:driver:" + userId + ":" + date;
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
            // 使用 StatisticsClient 调用相关服务获取司机统计数据
            // 这里可以调用多个服务获取不同维度的司机数据
            // 例如：考勤数据、行程数据、违规数据等
            // 为简化实现，暂时使用默认值
            entity.setAttendanceDays(0);
            entity.setAttendanceHours(BigDecimal.ZERO);
            entity.setTripCount(0);
            entity.setTotalDistance(BigDecimal.ZERO);
            entity.setCargoWeight(BigDecimal.ZERO);
            entity.setLateCount(0);
            entity.setEarlyLeaveCount(0);
            entity.setWarningCount(0);
            entity.setViolationCount(0);
            entity.setOverSpeedCount(0);
            entity.setRouteDeviationCount(0);
            entity.setPerformanceScore(BigDecimal.valueOf(100));
        } catch (Exception e) {
            log.warn("获取司机统计数据失败，使用默认值：{}", e.getMessage());
            entity.setAttendanceDays(0);
            entity.setAttendanceHours(BigDecimal.ZERO);
            entity.setTripCount(0);
            entity.setTotalDistance(BigDecimal.ZERO);
            entity.setCargoWeight(BigDecimal.ZERO);
            entity.setLateCount(0);
            entity.setEarlyLeaveCount(0);
            entity.setWarningCount(0);
            entity.setViolationCount(0);
            entity.setOverSpeedCount(0);
            entity.setRouteDeviationCount(0);
            entity.setPerformanceScore(BigDecimal.valueOf(100));
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        driverStatisticsMapper.insert(entity);
        log.info("计算司机统计数据：用户 ID={}, 日期={}, 行程数={}", userId, date, entity.getTripCount());
        
        DriverStatisticsVO vo = convertToDriverStatisticsVO(entity);
        
        // 写入缓存
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
            wrapper.ge(CostStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(CostStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(CostStatistics::getStatisticsDate);
        
        List<CostStatistics> list = costStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToCostStatisticsVO).collect(Collectors.toList());
    }

    @Override
    public List<VehicleStatisticsVO> getVehicleStatistics(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<VehicleStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getVehicleId() != null) {
            wrapper.eq(VehicleStatistics::getVehicleId, queryDTO.getVehicleId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(VehicleStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(VehicleStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(VehicleStatistics::getStatisticsDate);
        
        List<VehicleStatistics> list = vehicleStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToVehicleStatisticsVO).collect(Collectors.toList());
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
            
            // 这里可以继续调用其他服务获取车辆数量和司机数量
            // 为简化实现，暂时设为 0
            entity.setTotalVehicles(0);
            entity.setTotalDrivers(0);
            
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
    public void calculateFaultStatistics(Long vehicleId, String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        // 删除旧缓存
        String cacheKey = "statistics:fault:" + vehicleId + ":" + date;
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
            // 使用 StatisticsClient 调用 vehicle-service 获取故障统计数据
            java.util.Map<String, Object> faultStats = statisticsClient.getFaultStatistics(date, date);
            
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
        log.info("计算故障统计数据：车辆 ID={}, 日期={}, 故障数={}", vehicleId, date, entity.getFaultCount());
        
        FaultStatisticsVO vo = convertToFaultStatisticsVO(entity);
        
        // 写入缓存
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
}
