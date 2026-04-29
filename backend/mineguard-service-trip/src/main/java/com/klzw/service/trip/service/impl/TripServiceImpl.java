package com.klzw.service.trip.service.impl;

import java.util.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.client.CostClient;
import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.client.AiClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.result.Result;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.util.GeoUtils;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripEndDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.processor.TripStatusProcessor;
import com.klzw.service.trip.service.TripService;
import com.klzw.common.core.domain.dto.TripStatisticsResponseDTO;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripTrackVO;
import com.klzw.service.trip.vo.TripVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip> implements TripService {

    private final UserClient userClient;
    private final VehicleClient vehicleClient;
    private final DispatchClient dispatchClient;
    private final WarningClient warningClient;
    private final CostClient costClient;
    private final PythonClient pythonClient;
    private final AiClient aiClient;
    private final MessageClient messageClient;
    private final TripStatusProcessor tripStatusProcessor;
    private final com.klzw.service.trip.service.TripTrackService tripTrackService;
    private final com.klzw.common.redis.service.RedisCacheService redisCacheService;

    @Override
    public PageResult<TripVO> page(PageRequest pageRequest, Integer status) {
        Page<Trip> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Trip::getStatus, status);
        }
        wrapper.orderByDesc(Trip::getCreateTime);
        
        Page<Trip> result = getBaseMapper().selectPage(page, wrapper);
        List<TripVO> voList = result.getRecords()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public TripVO getById(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        return convertToVO(trip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TripDTO dto) {
        validateVehicleAndDriver(dto.getVehicleId(), dto.getDriverId());
        
        Trip trip = new Trip();
        BeanUtils.copyProperties(dto, trip);
        trip.setTripNo(generateTripNo());
        int oldStatus = -1; // 初始状态
        int newStatus = TripStatusEnum.PENDING.getCode(); // 待开始
        trip.setStatus(newStatus);
        trip.setDeleted(0);
        
        // 计算预计里程和时长
        try {
            GeoPoint startPoint = new GeoPoint(dto.getStartLongitude(), dto.getStartLatitude());
            GeoPoint endPoint = new GeoPoint(dto.getEndLongitude(), dto.getEndLatitude());
            double distance = GeoUtils.calculateDistance(startPoint, endPoint) / 1000; // 转换为公里
            trip.setEstimatedMileage(java.math.BigDecimal.valueOf(distance));
            trip.setEstimatedDuration((int) (distance / 60 * 60)); // 假设平均速度 60km/h
        } catch (Exception e) {
            log.warn("计算预计里程和时长失败", e);
        }
        
        getBaseMapper().insert(trip);
        log.info("创建行程成功，行程 ID：{}，行程编号：{}", trip.getId(), trip.getTripNo());
        
        // 处理状态变化
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
        
        return trip.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromDispatch(TripCreateRequest request) {
        log.info("根据调度任务创建行程：车辆 ID={}, 司机 ID={}, 调度任务 ID={}", 
            request.getVehicleId(), request.getDriverId(), request.getDispatchTaskId());
        
        try {
            validateVehicleAndDriver(request.getVehicleId(), request.getDriverId());
            
            Trip trip = new Trip();
            BeanUtils.copyProperties(request, trip);
            trip.setTripNo(generateTripNo());
            int oldStatus = -1;
            int newStatus = TripStatusEnum.PENDING.getCode();
            trip.setStatus(newStatus);
            trip.setDeleted(0);
            
            // 计算预计里程和时长
            try {
                GeoPoint startPoint = new GeoPoint(request.getStartLongitude(), request.getStartLatitude());
                GeoPoint endPoint = new GeoPoint(request.getEndLongitude(), request.getEndLatitude());
                double distance = GeoUtils.calculateDistance(startPoint, endPoint) / 1000;
                trip.setEstimatedMileage(java.math.BigDecimal.valueOf(distance));
                trip.setEstimatedDuration((int) (distance / 60 * 60));
            } catch (Exception e) {
                log.warn("计算预计里程和时长失败", e);
            }
            
            getBaseMapper().insert(trip);
            log.info("调度行程创建成功，行程 ID：{}，行程编号：{}, 调度任务 ID={}", 
                trip.getId(), trip.getTripNo(), request.getDispatchTaskId());
            
            tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
            
            return trip.getId();
        } catch (Exception e) {
            log.error("根据调度任务创建行程失败：{}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TripDTO dto) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.PENDING.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有待开始的行程可以修改");
        }
        
        validateVehicleAndDriver(dto.getVehicleId(), dto.getDriverId());
        
        BeanUtils.copyProperties(dto, trip);
        trip.setId(id);
        
        getBaseMapper().updateById(trip);
        log.info("更新行程成功，行程ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() == TripStatusEnum.IN_PROGRESS.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "进行中的行程不能删除");
        }
        
        getBaseMapper().deleteById(id);
        log.info("删除行程成功，行程ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTrip(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.PENDING.getCode() && trip.getStatus() != TripStatusEnum.ACCEPTED.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有待开始或已接单的行程可以开始");
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.IN_PROGRESS.getCode();
        trip.setStatus(newStatus);
        trip.setActualStartTime(LocalDateTime.now());
        
        getBaseMapper().updateById(trip);
        log.info("行程开始，行程ID：{}，调度任务ID：{}", id, trip.getDispatchTaskId());
        
        if (trip.getDispatchTaskId() != null) {
            try {
                dispatchClient.startTaskByTrip(trip.getDispatchTaskId());
                log.info("已通知dispatch模块更新任务状态：调度任务ID={}", trip.getDispatchTaskId());
            } catch (Exception e) {
                log.error("通知dispatch模块更新任务状态失败：调度任务ID={}，错误={}", trip.getDispatchTaskId(), e.getMessage());
            }
        }
        
        if (trip.getStartLongitude() != null && trip.getStartLatitude() != null &&
            trip.getEndLongitude() != null && trip.getEndLatitude() != null) {
            try {
                java.util.List<java.util.Map<String, Double>> routePoints = java.util.List.of(
                    java.util.Map.of("longitude", trip.getStartLongitude().doubleValue(), "latitude", trip.getStartLatitude().doubleValue()),
                    java.util.Map.of("longitude", trip.getEndLongitude().doubleValue(), "latitude", trip.getEndLatitude().doubleValue())
                );
                warningClient.setPlannedRoute(trip.getVehicleId(), routePoints);
                log.info("已设置规划路线：车辆ID={}", trip.getVehicleId());
            } catch (Exception e) {
                log.warn("设置规划路线失败：车辆ID={}，错误={}", trip.getVehicleId(), e.getMessage());
            }
        }
        
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTrip(Long id, TripEndDTO dto) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.IN_PROGRESS.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有进行中的行程可以结束");
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.COMPLETED.getCode(); // 已完成
        trip.setStatus(newStatus);
        trip.setActualEndTime(LocalDateTime.now());
        
        // 如果传入的终点坐标无效（为null或0），使用预计终点坐标
        Double validEndLongitude = dto.getEndLongitude();
        Double validEndLatitude = dto.getEndLatitude();
        if (dto.getEndLongitude() == null || dto.getEndLatitude() == null || 
            (dto.getEndLongitude() == 0.0 && dto.getEndLatitude() == 0.0)) {
            validEndLongitude = trip.getEndLongitude();
            validEndLatitude = trip.getEndLatitude();
            log.info("使用预计终点坐标：endLongitude={}, endLatitude={}", validEndLongitude, validEndLatitude);
        }
        trip.setEndLongitude(validEndLongitude);
        trip.setEndLatitude(validEndLatitude);
        
        // 计算实际时长
        if (trip.getActualStartTime() != null) {
            long durationMinutes = java.time.Duration.between(trip.getActualStartTime(), LocalDateTime.now()).toMinutes();
            trip.setActualDuration((int) durationMinutes);
        }
        
        // 先从轨迹数据计算实际里程
        double trackDistance = 0.0;
        try {
            trackDistance = tripTrackService.calculateTotalDistance(id);
            log.info("从轨迹数据计算里程：tripId={}, 里程={}公里", id, trackDistance);
        } catch (Exception e) {
            log.warn("从轨迹计算里程失败：tripId={}, error={}", id, e.getMessage());
        }
        
        // 设置实际里程（优先使用轨迹里程，如果轨迹里程为0则使用直线距离作为备用）
        if (trackDistance > 0) {
            trip.setActualMileage(java.math.BigDecimal.valueOf(trackDistance));
            log.info("使用轨迹里程作为实际里程：tripId={}, actualMileage={}公里", id, trackDistance);
        } else {
            // 备用方案：使用直线距离
            try {
                if (trip.getStartLongitude() != null && trip.getStartLatitude() != null &&
                    validEndLongitude != null && validEndLatitude != null) {
                    GeoPoint startPoint = new GeoPoint(trip.getStartLongitude(), trip.getStartLatitude());
                    GeoPoint endPoint = new GeoPoint(validEndLongitude, validEndLatitude);
                    double distance = GeoUtils.calculateDistance(startPoint, endPoint) / 1000; // 转换为公里
                    trip.setActualMileage(java.math.BigDecimal.valueOf(distance));
                    log.info("使用直线距离作为实际里程（备用）：tripId={}, actualMileage={}公里", id, distance);
                }
            } catch (Exception e) {
                log.warn("计算直线距离失败", e);
            }
        }
        
        // 计算平均速度（公里/小时）
        if (trip.getActualDuration() != null && trip.getActualDuration() > 0 && trip.getActualMileage() != null) {
            double averageSpeed = (trip.getActualMileage().doubleValue() / trip.getActualDuration()) * 60;
            trip.setAverageSpeed(averageSpeed);
        }
        
        // 计算实际佣金金额（假设每公里0.5元）
        try {
            if (trip.getActualMileage() != null) {
                java.math.BigDecimal commissionAmount = trip.getActualMileage().multiply(new java.math.BigDecimal("0.5"));
                trip.setEstimatedCommissionAmount(commissionAmount);
            }
        } catch (Exception e) {
            log.warn("计算实际佣金金额失败", e);
        }
        
        getBaseMapper().updateById(trip);
        log.info("结束行程成功，行程ID：{}", id);

        try {
            List<com.klzw.common.core.domain.dto.TripTrackDTO> tracks = tripTrackService.getTracksFromRedis(id);
            if (tracks != null && !tracks.isEmpty()) {
                tripTrackService.batchSaveTracks(tracks);
                log.info("行程结束，轨迹已持久化到MongoDB：行程ID={}, 轨迹点数={}", id, tracks.size());
            } else {
                log.info("行程结束，无轨迹数据需要持久化：行程ID={}", id);
                tripTrackService.deleteTracksFromRedis(id);
            }
            
            if (trip.getVehicleId() != null) {
                tripTrackService.deleteVehicleTripRelation(trip.getVehicleId());
                log.info("已清理车辆与行程的关联关系：车辆ID={}", trip.getVehicleId());
            }
        } catch (Exception e) {
            log.error("行程结束持久化轨迹失败，清理Redis数据：行程ID={}, 错误={}", id, e.getMessage());
            try {
                tripTrackService.deleteTracksFromRedis(id);
                if (trip.getVehicleId() != null) {
                    tripTrackService.deleteVehicleTripRelation(trip.getVehicleId());
                }
            } catch (Exception ignored) {
            }
        }
        
        // 停止预警检查
        try {
            warningClient.stopTripWarningCheck(id);
            log.info("已停止预警检查：行程ID={}", id);
        } catch (Exception e) {
            log.error("停止预警检查失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // 回调dispatch模块完成任务
        if (trip.getDispatchTaskId() != null) {
            try {
                dispatchClient.completeTaskByTrip(trip.getDispatchTaskId());
                log.info("已通知dispatch模块完成任务：调度任务ID={}", trip.getDispatchTaskId());
            } catch (Exception e) {
                log.error("通知dispatch模块完成任务失败：调度任务ID={}，错误={}", trip.getDispatchTaskId(), e.getMessage());
            }
        }
        
        // 创建成本明细记录
        try {
            createCostDetails(trip, dto.getTolls(), dto.getTollDistance());
        } catch (Exception e) {
            log.error("创建成本明细记录失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // Python服务计算行程得分并更新司机分数
        try {
            var scoreResult = pythonClient.analyzeDrivingBehavior(id);
            if (scoreResult != null && scoreResult.isSuccess() && scoreResult.getData() != null) {
                int pythonScore = scoreResult.getData();
                log.info("Python服务计算行程得分完成：行程ID={}, 得分={}", id, pythonScore);
                
                // 更新司机分数
                if (trip.getDriverId() != null && trip.getActualMileage() != null) {
                    double tripDistance = trip.getActualMileage().doubleValue();
                    var updateResult = userClient.updateDriverScoreFromTrip(trip.getDriverId(), pythonScore, tripDistance);
                    if (updateResult != null && updateResult.isSuccess()) {
                        log.info("司机分数更新成功：司机ID={}, 行程得分={}", trip.getDriverId(), updateResult.getData());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Python服务计算行程得分失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // AI服务分析行程并推送意见
        try {
            Map<String, Object> tripData = new java.util.HashMap<>();
            tripData.put("tripId", id);
            tripData.put("driverId", trip.getDriverId());
            tripData.put("vehicleId", trip.getVehicleId());
            tripData.put("actualMileage", trip.getActualMileage());
            tripData.put("actualDuration", trip.getActualDuration());
            tripData.put("averageSpeed", trip.getAverageSpeed());
            
            // 获取该行程的预警记录
            try {
                var warningResult = warningClient.getRecordsByTripId(id);
                if (warningResult != null && warningResult.isSuccess() && warningResult.getData() != null) {
                    tripData.put("warningRecords", warningResult.getData());
                    log.info("获取行程预警记录成功：行程ID={}, 预警数量={}", id, warningResult.getData().size());
                }
            } catch (Exception warnEx) {
                log.warn("获取行程预警记录失败：行程ID={}, 错误={}", id, warnEx.getMessage());
            }
            
            var result = aiClient.analyzeDrivingBehavior(tripData);
            if (result != null && result.isSuccess() && result.getData() != null) {
                // 保存AI分析结果到Trip表
                String aiAnalysisJson = com.klzw.common.core.util.JsonUtils.toJson(result.getData());
                trip.setAiAnalysis(aiAnalysisJson);
                this.updateById(trip);
                log.info("AI服务分析行程完成并保存结果：行程ID={}", id);
                
                // 推送消息给管理端
                try {
                    messageClient.sendMessageByRole(
                        "admin",
                        "行程分析报告",
                        String.format("行程 %s 已完成AI分析，请查看行程详情获取分析报告", trip.getTripNo()),
                        "trip_analysis"
                    );
                    log.info("已推送行程分析报告给管理端：行程ID={}", id);
                } catch (Exception msgEx) {
                    log.error("推送行程分析报告失败：行程ID={}，错误={}", id, msgEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("AI服务分析行程失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // 处理状态变化（车辆状态更新 + 通知）
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }
    
    /**
     * 创建成本明细记录
     */
    private void createCostDetails(Trip trip, java.math.BigDecimal tolls, Integer tollDistance) {
        if (trip.getActualMileage() == null || trip.getVehicleId() == null) {
            return;
        }
        
        // 获取车辆信息，包括折旧系数
        try {
            var vehicleResult = vehicleClient.getById(trip.getVehicleId());
            if (vehicleResult == null || !vehicleResult.isSuccess()) {
                log.warn("获取车辆信息失败：车辆ID={}", trip.getVehicleId());
                return;
            }
            
            java.util.Map<String, Object> vehicleData = vehicleResult.getData();
            if (vehicleData == null) {
                log.warn("车辆信息为空：车辆ID={}", trip.getVehicleId());
                return;
            }
            
            // 创建车辆折旧成本明细
            Object depreciationRateObj = vehicleData.get("depreciationRate");
            java.math.BigDecimal depreciationRate = null;
            if (depreciationRateObj instanceof java.math.BigDecimal) {
                depreciationRate = (java.math.BigDecimal) depreciationRateObj;
            } else if (depreciationRateObj instanceof Number) {
                depreciationRate = java.math.BigDecimal.valueOf(((Number) depreciationRateObj).doubleValue());
            }
            if (depreciationRate != null && depreciationRate.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal depreciationAmount = trip.getActualMileage().multiply(depreciationRate).setScale(2, java.math.RoundingMode.HALF_UP);
                
                Map<String, Object> depreciationCost = new java.util.HashMap<>();
                depreciationCost.put("costType", 5); // 折旧成本
                depreciationCost.put("costName", "车辆折旧");
                depreciationCost.put("amount", depreciationAmount);
                depreciationCost.put("vehicleId", trip.getVehicleId());
                depreciationCost.put("tripId", trip.getId());
                depreciationCost.put("costDate", java.time.LocalDate.now());
                depreciationCost.put("remark", "行程ID: " + trip.getId() + ", 里程: " + trip.getActualMileage() + "公里, 折旧系数: " + depreciationRate);
                
                costClient.addCostDetail(depreciationCost);
                log.info("创建车辆折旧成本明细成功：行程ID={}, 金额={}", trip.getId(), depreciationAmount);
            }
        } catch (Exception e) {
            log.error("创建车辆折旧成本明细失败：行程ID={}，错误={}", trip.getId(), e.getMessage());
        }
        
        // 创建行程提成成本明细
        try {
            if (trip.getEstimatedCommissionAmount() != null && trip.getDriverId() != null) {
                Map<String, Object> commissionCost = new java.util.HashMap<>();
                commissionCost.put("costType", 8); // 行程提成
                commissionCost.put("costName", "行程提成");
                commissionCost.put("amount", trip.getEstimatedCommissionAmount());
                commissionCost.put("userId", trip.getDriverId());
                commissionCost.put("tripId", trip.getId());
                commissionCost.put("costDate", java.time.LocalDate.now());
                commissionCost.put("remark", "行程ID: " + trip.getId() + ", 里程: " + trip.getActualMileage() + "公里");
                
                costClient.addCostDetail(commissionCost);
                log.info("创建行程提成成本明细成功：行程ID={}, 金额={}", trip.getId(), trip.getEstimatedCommissionAmount());
            }
        } catch (Exception e) {
            log.error("创建行程提成成本明细失败：行程ID={}，错误={}", trip.getId(), e.getMessage());
        }
        
        // 创建过路费成本明细
        try {
            if (tolls != null && tolls.compareTo(java.math.BigDecimal.ZERO) > 0) {
                Map<String, Object> tollCost = new java.util.HashMap<>();
                tollCost.put("costType", 6); // 过路费
                tollCost.put("costName", "过路费");
                tollCost.put("amount", tolls);
                tollCost.put("vehicleId", trip.getVehicleId());
                tollCost.put("tripId", trip.getId());
                tollCost.put("costDate", java.time.LocalDate.now());
                String remark = "行程ID: " + trip.getId() + ", 过路费: " + tolls + "元";
                if (tollDistance != null && tollDistance > 0) {
                    remark += ", 收费路段距离: " + tollDistance + "米";
                }
                tollCost.put("remark", remark);
                
                costClient.addCostDetail(tollCost);
                log.info("创建过路费成本明细成功：行程ID={}, 金额={}元", trip.getId(), tolls);
            }
        } catch (Exception e) {
            log.error("创建过路费成本明细失败：行程ID={}，错误={}", trip.getId(), e.getMessage());
        }
    }

    @Override
    public List<TripVO> getByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getVehicleId, vehicleId);
        wrapper.orderByDesc(Trip::getCreateTime);
        
        List<Trip> trips = getBaseMapper().selectList(wrapper);
        return trips.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripVO> getByDriverId(Long driverId) {
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getDriverId, driverId);
        wrapper.orderByDesc(Trip::getCreateTime);
        
        List<Trip> trips = getBaseMapper().selectList(wrapper);
        return trips.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public TripVO getByTripNo(String tripNo) {
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getTripNo, tripNo);
        
        Trip trip = getBaseMapper().selectOne(wrapper);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        return convertToVO(trip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptTrip(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.PENDING.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有待开始的行程可以接单");
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.ACCEPTED.getCode(); // 已接单
        trip.setStatus(newStatus);
        
        getBaseMapper().updateById(trip);
        log.info("接单成功，行程ID：{}", id);
        
        // 处理状态变化
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }
    
    @Override
    public TripResponse getLatestTripByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getVehicleId, vehicleId);
        wrapper.orderByDesc(Trip::getCreateTime);
        wrapper.last("LIMIT 1");
        
        Trip trip = getBaseMapper().selectOne(wrapper);
        TripResponse response = new TripResponse();
        
        if (trip != null) {
            response.setId(trip.getId());
            response.setVehicleId(trip.getVehicleId());
            response.setDriverId(trip.getDriverId());
            response.setTripNo(trip.getTripNo());
            response.setStatus(trip.getStatus());
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseTrip(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.IN_PROGRESS.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有进行中的行程可以暂停");
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.PAUSED.getCode(); // 暂停中
        trip.setStatus(newStatus);
        
        getBaseMapper().updateById(trip);
        log.info("暂停行程成功，行程ID：{}", id);
        
        // 处理状态变化
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeTrip(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() != TripStatusEnum.PAUSED.getCode()) {
            throw new TripException(TripResultCode.TRIP_STATUS_ERROR, "只有暂停中的行程可以恢复");
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.IN_PROGRESS.getCode(); // 进行中
        trip.setStatus(newStatus);
        
        getBaseMapper().updateById(trip);
        log.info("恢复行程成功，行程ID：{}", id);
        
        // 处理状态变化
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }

    private void validateVehicleAndDriver(Long vehicleId, Long driverId) {
        com.klzw.common.core.result.Result<java.lang.Boolean> vehicleExistsResult = vehicleClient.existsById(vehicleId);
        Boolean vehicleExists = vehicleExistsResult != null && vehicleExistsResult.getCode() == 200 ? vehicleExistsResult.getData() : false;
        if (!vehicleExists) {
            throw new TripException(TripResultCode.VEHICLE_NOT_AVAILABLE, "车辆不存在或不可用");
        }

        com.klzw.common.core.result.Result<com.klzw.common.core.domain.dto.VehicleStatus> vehicleStatusResult = vehicleClient.getStatus(vehicleId);
        if (vehicleStatusResult != null && vehicleStatusResult.getCode() == 200 && vehicleStatusResult.getData() != null) {
            int vehicleStatusCode = vehicleStatusResult.getData().getStatus();
            if (vehicleStatusCode != com.klzw.common.core.enums.VehicleStatusEnum.IDLE.getCode()) {
                String statusName = com.klzw.common.core.enums.VehicleStatusEnum.getByCode(vehicleStatusCode) != null
                        ? com.klzw.common.core.enums.VehicleStatusEnum.getByCode(vehicleStatusCode).getName()
                        : "未知";
                throw new TripException(TripResultCode.VEHICLE_NOT_AVAILABLE,
                        "车辆当前状态为[" + statusName + "]，非空闲状态，无法创建行程");
            }
        } else {
            log.warn("获取车辆状态失败，跳过状态检查：车辆ID={}", vehicleId);
        }
        
        com.klzw.common.core.result.Result<java.lang.Boolean> driverExistsResult = userClient.existsUser(driverId);
        Boolean driverExists = driverExistsResult != null && driverExistsResult.getCode() == 200 ? driverExistsResult.getData() : false;
        if (!driverExists) {
            throw new TripException(TripResultCode.DRIVER_NOT_AVAILABLE, "司机不存在或不可用");
        }
    }

    private String generateTripNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "TRIP" + timestamp + random;
    }

    private TripVO convertToVO(Trip trip) {
        TripVO vo = new TripVO();
        BeanUtils.copyProperties(trip, vo);
        vo.setId(trip.getId() != null ? trip.getId().toString() : null);
        vo.setVehicleId(trip.getVehicleId() != null ? trip.getVehicleId().toString() : null);
        vo.setDriverId(trip.getDriverId() != null ? trip.getDriverId().toString() : null);
        vo.setEstimatedMileage(trip.getEstimatedMileage() != null ? trip.getEstimatedMileage().doubleValue() : null);
        vo.setActualMileage(trip.getActualMileage() != null ? trip.getActualMileage().doubleValue() : null);
        
        try {
            var vehicleInfoResult = vehicleClient.getById(trip.getVehicleId());
            if (vehicleInfoResult != null && vehicleInfoResult.getCode() == 200 && vehicleInfoResult.getData() != null) {
                java.util.Map<String, Object> vehicleData = vehicleInfoResult.getData();
                vo.setVehicleNo((String) vehicleData.get("vehicleNo"));
            }
        } catch (Exception e) {
            log.warn("获取车辆信息失败，vehicleId={}", trip.getVehicleId(), e);
        }
        
        try {
            var userInfo = userClient.getUserById(trip.getDriverId());
            if (userInfo != null && userInfo.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userData = (java.util.Map<String, Object>) userInfo.getData();
                vo.setDriverName((String) userData.get("realName"));
            }
        } catch (Exception e) {
            log.warn("获取司机信息失败，driverId={}", trip.getDriverId(), e);
        }
        
        // 从调度任务获取优先级和截止日期
        try {
            if (trip.getDispatchTaskId() != null) {
                var taskDetailResult = dispatchClient.getTaskDetail(trip.getDispatchTaskId());
                if (taskDetailResult != null && taskDetailResult.getCode() == 200 && taskDetailResult.getData() != null) {
                    var taskDetail = taskDetailResult.getData();
                    // 设置优先级
                    if (taskDetail.containsKey("priority")) {
                        vo.setPriority((String) taskDetail.get("priority"));
                    }
                    // 设置截止日期
                    if (taskDetail.containsKey("scheduledEndTime")) {
                        vo.setDeadline((java.time.LocalDateTime) taskDetail.get("scheduledEndTime"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取调度任务信息失败，dispatchTaskId={}", trip.getDispatchTaskId(), e);
        }
        
        // 设置AI分析结果
        vo.setAiAnalysis(trip.getAiAnalysis());
        
        // 设置预计提成金额
        vo.setEstimatedCommissionAmount(trip.getEstimatedCommissionAmount());
        
        return vo;
    }

    @Override
    public TripStatisticsVO getTripStatistics(Long tripId) {
        Trip trip = getBaseMapper().selectById(tripId);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }

        TripStatisticsVO vo = new TripStatisticsVO();
        vo.setTripId(tripId);

        if (trip.getActualStartTime() != null && trip.getActualEndTime() != null) {
            long durationMinutes = java.time.Duration.between(trip.getActualStartTime(), trip.getActualEndTime()).toMinutes();
            vo.setDurationMinutes(durationMinutes);
        }

        vo.setEstimatedDistance(trip.getEstimatedMileage());

        BigDecimal actualDistance = calculateActualDistance(tripId);
        vo.setActualDistance(actualDistance);

        if (vo.getDurationMinutes() != null && vo.getDurationMinutes() > 0 && actualDistance != null) {
            double hours = vo.getDurationMinutes() / 60.0;
            vo.setAverageSpeed(actualDistance.doubleValue() / hours);
        }

        return vo;
    }
    
    @Override
    public TripStatisticsResponseDTO getStatisticsByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Trip::getCreateTime, start.atStartOfDay())
               .le(Trip::getCreateTime, end.atTime(23, 59, 59));
        
        List<Trip> trips = this.list(wrapper);
        
        TripStatisticsResponseDTO dto = new TripStatisticsResponseDTO();
        
        if (trips == null || trips.isEmpty()) {
            dto.setTripCount(0);
            dto.setTotalDistance(BigDecimal.ZERO);
            dto.setTotalDuration(BigDecimal.ZERO);
            dto.setCompletedTripCount(0);
            dto.setCancelledTripCount(0);
            dto.setAverageSpeed(BigDecimal.ZERO);
            dto.setFuelConsumption(BigDecimal.ZERO);
            dto.setCargoWeight(BigDecimal.ZERO);
            return dto;
        }
        
        int tripCount = trips.size();
        int completedCount = 0;
        int cancelledCount = 0;
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalDuration = BigDecimal.ZERO;
        BigDecimal totalCargoWeight = BigDecimal.ZERO;
        
        for (Trip trip : trips) {
            if (trip.getStatus() != null && TripStatusEnum.COMPLETED.getCode() == trip.getStatus()) {
                completedCount++;
            } else if (trip.getStatus() != null && TripStatusEnum.CANCELLED.getCode() == trip.getStatus()) {
                cancelledCount++;
            }
            
            if (trip.getActualMileage() != null) {
                totalDistance = totalDistance.add(trip.getActualMileage());
            } else if (trip.getEstimatedMileage() != null) {
                totalDistance = totalDistance.add(trip.getEstimatedMileage());
            }
            
            if (trip.getActualStartTime() != null && trip.getActualEndTime() != null) {
                long minutes = Duration.between(trip.getActualStartTime(), trip.getActualEndTime()).toMinutes();
                totalDuration = totalDuration.add(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
            }
            
            if (trip.getCargoWeight() != null) {
                totalCargoWeight = totalCargoWeight.add(trip.getCargoWeight());
            } else if (trip.getActualCargoWeight() != null) {
                totalCargoWeight = totalCargoWeight.add(trip.getActualCargoWeight());
            }
        }
        
        BigDecimal averageSpeed = BigDecimal.ZERO;
        if (totalDuration.compareTo(BigDecimal.ZERO) > 0) {
            averageSpeed = totalDistance.divide(totalDuration, 2, java.math.RoundingMode.HALF_UP);
        }
        
        dto.setTripCount(tripCount);
        dto.setTotalDistance(totalDistance);
        dto.setTotalDuration(totalDuration);
        dto.setCompletedTripCount(completedCount);
        dto.setCancelledTripCount(cancelledCount);
        dto.setAverageSpeed(averageSpeed);
        dto.setFuelConsumption(BigDecimal.ZERO);
        dto.setCargoWeight(totalCargoWeight);
        
        log.info("按日期范围查询行程统计：startDate={}, endDate={}, tripCount={}", startDate, endDate, tripCount);
        
        return dto;
    }

    private BigDecimal calculateActualDistance(Long tripId) {
        // 简化实现，直接返回估计里程
        Trip trip = getBaseMapper().selectById(tripId);
        if (trip == null) {
            return null;
        }
        return trip.getEstimatedMileage();
    }

    @Override
    public List<com.klzw.service.trip.vo.TripTrackVO> getTracksByTripId(Long tripId) {
        return tripTrackService.getByTripId(tripId);
    }
    
    @Override
    public TripVO getActiveTrip() {
        // 获取当前登录用户的ID
        Long driverId = com.klzw.common.auth.context.UserContext.getUserId();
        if (driverId == null) {
            return null;
        }
        
        // 查询该用户的进行中行程
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getDriverId, driverId);
        wrapper.eq(Trip::getStatus, TripStatusEnum.IN_PROGRESS.getCode());
        
        Trip trip = getBaseMapper().selectOne(wrapper);
        if (trip == null) {
            return null;
        }
        
        TripVO vo = convertToVO(trip);
        
        // 从 Redis 中获取行程的最后一次存储位置
        try {
            String trackKey = "trip:track:" + trip.getId();
            List<?> trackList = redisCacheService.lRange(trackKey, -1L, -1L);
            if (trackList != null && !trackList.isEmpty()) {
                Object lastTrack = trackList.get(0);
                if (lastTrack instanceof java.util.Map) {
                    java.util.Map<?, ?> trackMap = (java.util.Map<?, ?>) lastTrack;
                    Object longitude = trackMap.get("longitude");
                    Object latitude = trackMap.get("latitude");
                    Object recordTime = trackMap.get("recordTime");
                    
                    if (longitude != null && latitude != null) {
                        vo.setLastLongitude(Double.parseDouble(longitude.toString()));
                        vo.setLastLatitude(Double.parseDouble(latitude.toString()));
                    }
                    
                    if (recordTime != null) {
                        vo.setLastRecordTime(LocalDateTime.parse(recordTime.toString()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取行程最后位置失败：tripId={}, error={}", trip.getId(), e.getMessage());
        }
        
        return vo;
    }
    
    @Override
    public TripVO getCurrentTripByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getDriverId, userId);
        wrapper.in(Trip::getStatus, TripStatusEnum.IN_PROGRESS.getCode(), TripStatusEnum.PAUSED.getCode());
        wrapper.orderByDesc(Trip::getCreateTime);
        wrapper.last("LIMIT 1");
        
        Trip trip = getBaseMapper().selectOne(wrapper);
        if (trip == null) {
            return null;
        }
        
        TripVO vo = convertToVO(trip);
        
        try {
            String trackKey = "trip:track:" + trip.getId();
            List<?> trackList = redisCacheService.lRange(trackKey, -1L, -1L);
            if (trackList != null && !trackList.isEmpty()) {
                Object lastTrack = trackList.get(0);
                if (lastTrack instanceof java.util.Map) {
                    java.util.Map<?, ?> trackMap = (java.util.Map<?, ?>) lastTrack;
                    Object longitude = trackMap.get("longitude");
                    Object latitude = trackMap.get("latitude");
                    
                    if (longitude != null && latitude != null) {
                        vo.setLastLongitude(Double.parseDouble(longitude.toString()));
                        vo.setLastLatitude(Double.parseDouble(latitude.toString()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取行程最后位置失败：tripId={}, error={}", trip.getId(), e.getMessage());
        }
        
        return vo;
    }

    public List<Map<String, Object>> getTripWarningRecords(Long tripId) {
        log.info("获取行程预警记录：tripId={}", tripId);
        try {
            var result = warningClient.getRecordsByTripId(tripId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取行程预警记录失败：tripId={}, error={}", tripId, e.getMessage());
        }
        return List.of();
    }

    @Override
    public TripVO getTripDetail(Long id) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            return null;
        }
        return convertToVO(trip);
    }

    @Override
    public List<Map<String, Object>> getTripCostDetails(Long tripId) {
        log.info("获取行程成本明细：tripId={}", tripId);
        try {
            Result<List<Map<String, Object>>> result = costClient.getCostDetailList(tripId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取行程成本明细失败：tripId={}, error={}", tripId, e.getMessage());
        }
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTrip(Long id, String reason) {
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new TripException(TripResultCode.TRIP_NOT_FOUND);
        }
        
        if (trip.getStatus() == TripStatusEnum.COMPLETED.getCode() || trip.getStatus() == TripStatusEnum.CANCELLED.getCode()) {
            log.warn("行程已完成或已取消，无法取消：tripId={}, status={}", id, trip.getStatus());
            return;
        }
        
        int oldStatus = trip.getStatus();
        int newStatus = TripStatusEnum.CANCELLED.getCode();
        trip.setStatus(newStatus);
        trip.setCancellationReason(reason);
        trip.setUpdateTime(LocalDateTime.now());
        
        getBaseMapper().updateById(trip);
        log.info("取消行程成功，行程ID：{}，原因：{}", id, reason);
        
        // 清理Redis中的轨迹数据和车辆关联关系
        try {
            tripTrackService.deleteTracksFromRedis(id);
            if (trip.getVehicleId() != null) {
                tripTrackService.deleteVehicleTripRelation(trip.getVehicleId());
                log.info("已清理车辆与行程的关联关系：车辆ID={}", trip.getVehicleId());
            }
        } catch (Exception e) {
            log.error("清理Redis数据失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // 停止预警检查
        try {
            warningClient.stopTripWarningCheck(id);
            log.info("已停止预警检查：行程ID={}", id);
        } catch (Exception e) {
            log.error("停止预警检查失败：行程ID={}，错误={}", id, e.getMessage());
        }
        
        // 处理状态变化（车辆状态更新 + 通知）
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTripByDispatchTaskId(Long dispatchTaskId, String reason) {
        log.info("根据调度任务ID取消行程：dispatchTaskId={}, reason={}", dispatchTaskId, reason);
        
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getDispatchTaskId, dispatchTaskId);
        wrapper.ne(Trip::getStatus, TripStatusEnum.COMPLETED.getCode());
        wrapper.ne(Trip::getStatus, TripStatusEnum.CANCELLED.getCode());
        
        Trip trip = getBaseMapper().selectOne(wrapper);
        if (trip != null) {
            cancelTrip(trip.getId(), reason);
            log.info("已取消关联行程：tripId={}, dispatchTaskId={}", trip.getId(), dispatchTaskId);
        } else {
            log.info("未找到需要取消的行程：dispatchTaskId={}", dispatchTaskId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActualCargoWeight(Long id, java.math.BigDecimal actualCargoWeight) {
        log.info("更新实际货物重量：tripId={}, actualCargoWeight={}", id, actualCargoWeight);
        
        Trip trip = getBaseMapper().selectById(id);
        if (trip == null) {
            throw new RuntimeException("行程不存在：id=" + id);
        }
        
        trip.setActualCargoWeight(actualCargoWeight);
        getBaseMapper().updateById(trip);
        
        log.info("实际货物重量已更新：tripId={}", id);
    }

    @Override
    public java.util.Map<String, Object> getDriverStatistics(Long driverId, String startDate, String endDate) {
        log.info("获取司机行程统计：driverId={}, startDate={}, endDate={}", driverId, startDate, endDate);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Trip> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(Trip::getDriverId, driverId)
                   .isNotNull(Trip::getActualEndTime)
                   .ge(Trip::getActualEndTime, start.atStartOfDay())
                   .le(Trip::getActualEndTime, end.atTime(23, 59, 59));
            
            List<Trip> trips = getBaseMapper().selectList(wrapper);
            
            int tripCount = trips.size();
            java.math.BigDecimal totalDistance = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalDuration = java.math.BigDecimal.ZERO;
            java.math.BigDecimal cargoWeight = java.math.BigDecimal.ZERO;
            
            for (Trip trip : trips) {
                if (trip.getActualMileage() != null) {
                    totalDistance = totalDistance.add(trip.getActualMileage());
                } else if (trip.getEstimatedMileage() != null) {
                    totalDistance = totalDistance.add(trip.getEstimatedMileage());
                }
                if (trip.getActualDuration() != null) {
                    totalDuration = totalDuration.add(java.math.BigDecimal.valueOf(trip.getActualDuration()));
                }
                if (trip.getCargoWeight() != null) {
                    cargoWeight = cargoWeight.add(trip.getCargoWeight());
                } else if (trip.getActualCargoWeight() != null) {
                    cargoWeight = cargoWeight.add(trip.getActualCargoWeight());
                }
            }
            
            result.put("driverId", driverId);
            result.put("tripCount", tripCount);
            result.put("totalDistance", totalDistance);
            result.put("totalDuration", totalDuration);
            result.put("cargoWeight", cargoWeight);
            
            log.info("司机行程统计完成：driverId={}, tripCount={}, totalDistance={}, cargoWeight={}", driverId, tripCount, totalDistance, cargoWeight);
            
        } catch (Exception e) {
            log.error("获取司机行程统计失败：driverId={}", driverId, e);
            result.put("driverId", driverId);
            result.put("tripCount", 0);
            result.put("totalDistance", java.math.BigDecimal.ZERO);
            result.put("totalDuration", java.math.BigDecimal.ZERO);
            result.put("cargoWeight", java.math.BigDecimal.ZERO);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public java.util.Map<String, Object> getVehicleTripStatistics(Long vehicleId, String startDate, String endDate) {
        log.info("获取车辆行程统计：vehicleId={}, startDate={}, endDate={}", vehicleId, startDate, endDate);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            // 获取车辆创建时间，计算运营天数
            long operatingDays = 0L;
            try {
                var vehicleResult = vehicleClient.getById(vehicleId);
                if (vehicleResult != null && vehicleResult.getCode() == 200 && vehicleResult.getData() != null) {
                    java.util.Map<String, Object> vehicleData = vehicleResult.getData();
                    Object createTimeObj = vehicleData.get("createTime");
                    if (createTimeObj != null) {
                        java.time.LocalDateTime createTime = parseDateTime(createTimeObj);
                        if (createTime != null) {
                            operatingDays = java.time.temporal.ChronoUnit.DAYS.between(createTime.toLocalDate(), java.time.LocalDate.now()) + 1;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取车辆创建时间失败：vehicleId={}", vehicleId);
            }
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Trip> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(Trip::getVehicleId, vehicleId);
            
            if (startDate != null && endDate != null) {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                wrapper.ge(Trip::getCreateTime, start.atStartOfDay())
                       .le(Trip::getCreateTime, end.atTime(23, 59, 59));
            }
            
            List<Trip> trips = getBaseMapper().selectList(wrapper);
            
            java.math.BigDecimal totalMileage = java.math.BigDecimal.ZERO;
            for (Trip trip : trips) {
                if (trip.getActualMileage() != null) {
                    totalMileage = totalMileage.add(trip.getActualMileage());
                } else if (trip.getEstimatedMileage() != null) {
                    totalMileage = totalMileage.add(trip.getEstimatedMileage());
                }
            }
            
            java.math.BigDecimal totalDuration = java.math.BigDecimal.ZERO;
            for (Trip trip : trips) {
                if (trip.getActualDuration() != null) {
                    totalDuration = totalDuration.add(java.math.BigDecimal.valueOf(trip.getActualDuration()));
                }
            }
            
            java.math.BigDecimal totalCargoWeight = trips.stream()
                .filter(t -> t.getActualCargoWeight() != null || t.getCargoWeight() != null)
                .map(t -> t.getActualCargoWeight() != null ? t.getActualCargoWeight() : t.getCargoWeight())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            result.put("vehicleId", vehicleId);
            result.put("operatingDays", operatingDays);
            result.put("totalMileage", totalMileage);
            result.put("totalDuration", totalDuration);
            result.put("totalCargoWeight", totalCargoWeight);
            result.put("tripCount", trips.size());
            
            log.info("车辆行程统计完成：vehicleId={}, operatingDays={}, totalMileage={}, tripCount={}", 
                vehicleId, operatingDays, totalMileage, trips.size());
            
        } catch (Exception e) {
            log.error("获取车辆行程统计失败：vehicleId={}", vehicleId, e);
            result.put("vehicleId", vehicleId);
            result.put("operatingDays", 0L);
            result.put("totalMileage", java.math.BigDecimal.ZERO);
            result.put("totalDuration", java.math.BigDecimal.ZERO);
            result.put("totalCargoWeight", java.math.BigDecimal.ZERO);
            result.put("tripCount", 0);
            result.put("totalFuelConsumption", java.math.BigDecimal.ZERO);
            result.put("totalIdleDuration", java.math.BigDecimal.ZERO);
            result.put("totalIdleDistance", java.math.BigDecimal.ZERO);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public java.util.List<java.util.Map<String, Object>> getWeeklyOperationStats() {
        log.info("获取每周运营统计");
        
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        try {
            LocalDate today = LocalDate.now();
            String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            
            // 获取本周的周一
            LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = monday.plusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.atTime(23, 59, 59);
                
                // 查询当天有行程的车辆数
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Trip> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.ge(Trip::getActualStartTime, dayStart)
                       .le(Trip::getActualStartTime, dayEnd)
                       .isNotNull(Trip::getVehicleId);
                
                List<Trip> trips = getBaseMapper().selectList(wrapper);
                
                // 统计不重复的车辆数
                long vehicleCount = trips.stream()
                    .map(Trip::getVehicleId)
                    .distinct()
                    .count();
                
                java.util.Map<String, Object> dayStat = new java.util.HashMap<>();
                dayStat.put("label", dayNames[i]);
                dayStat.put("value", vehicleCount);
                dayStat.put("date", date.toString());
                
                result.add(dayStat);
            }
            
            log.info("每周运营统计完成");
            
        } catch (Exception e) {
            log.error("获取每周运营统计失败", e);
        }
        
        return result;
    }
    
    private java.time.LocalDateTime parseDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) obj;
        }
        if (obj instanceof String) {
            try {
                return java.time.LocalDateTime.parse((String) obj);
            } catch (Exception e) {
                return null;
            }
        }
        if (obj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) obj;
            if (list.size() >= 6) {
                try {
                    int year = ((Number) list.get(0)).intValue();
                    int month = ((Number) list.get(1)).intValue();
                    int day = ((Number) list.get(2)).intValue();
                    int hour = ((Number) list.get(3)).intValue();
                    int minute = ((Number) list.get(4)).intValue();
                    int second = ((Number) list.get(5)).intValue();
                    return java.time.LocalDateTime.of(year, month, day, hour, minute, second);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}