package com.klzw.service.trip.service.impl;

import java.util.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.util.GeoUtils;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.processor.TripStatusProcessor;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.dto.TripStatisticsResponseDTO;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip> implements TripService {

    private final UserClient userClient;
    private final VehicleClient vehicleClient;
    private final DispatchClient dispatchClient;
    private final TripStatusProcessor tripStatusProcessor;
    private final com.klzw.service.trip.service.TripTrackService tripTrackService;

    @Override
    public PageResult<TripVO> page(PageRequest pageRequest) {
        Page<Trip> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
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
        int newStatus = TripStatusEnum.IN_PROGRESS.getCode(); // 进行中
        trip.setStatus(newStatus);
        trip.setActualStartTime(LocalDateTime.now());
        
        getBaseMapper().updateById(trip);
        log.info("行程开始，行程ID：{}，调度任务ID：{}", id, trip.getDispatchTaskId());
        
        // 回调dispatch模块更新调度任务状态为进行中
        if (trip.getDispatchTaskId() != null) {
            try {
                dispatchClient.startTaskByTrip(trip.getDispatchTaskId());
                log.info("已通知dispatch模块更新任务状态：调度任务ID={}", trip.getDispatchTaskId());
            } catch (Exception e) {
                log.error("通知dispatch模块更新任务状态失败：调度任务ID={}，错误={}", trip.getDispatchTaskId(), e.getMessage());
            }
        }
        
        // 处理状态变化（车辆状态更新 + 通知）
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTrip(Long id, Double endLongitude, Double endLatitude) {
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
        trip.setEndLongitude(endLongitude);
        trip.setEndLatitude(endLatitude);
        
        // 计算实际里程和时长
        try {
            GeoPoint startPoint = new GeoPoint(trip.getStartLongitude(), trip.getStartLatitude());
            GeoPoint endPoint = new GeoPoint(endLongitude, endLatitude);
            double distance = GeoUtils.calculateDistance(startPoint, endPoint) / 1000; // 转换为公里
            trip.setActualMileage(java.math.BigDecimal.valueOf(distance));
            
            if (trip.getActualStartTime() != null) {
                long durationMinutes = java.time.Duration.between(trip.getActualStartTime(), LocalDateTime.now()).toMinutes();
                trip.setActualDuration((int) durationMinutes);
            }
        } catch (Exception e) {
            log.warn("计算实际里程和时长失败", e);
        }
        
        getBaseMapper().updateById(trip);
        log.info("结束行程成功，行程ID：{}", id);

        try {
            List<com.klzw.service.trip.dto.TripTrackDTO> tracks = tripTrackService.getTracksFromRedis(id);
            if (tracks != null && !tracks.isEmpty()) {
                tripTrackService.batchSaveTracks(tracks);
                log.info("行程结束，轨迹已持久化到MongoDB：行程ID={}, 轨迹点数={}", id, tracks.size());
            } else {
                log.info("行程结束，无轨迹数据需要持久化：行程ID={}", id);
            }
        } catch (Exception e) {
            log.error("行程结束持久化轨迹失败（不影响主流程）：行程ID={}, 错误={}", id, e.getMessage());
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
        
        // 处理状态变化（车辆状态更新 + 通知）
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
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
        
        try {
            var vehicleInfoResult = vehicleClient.getById(trip.getVehicleId());
            if (vehicleInfoResult != null && vehicleInfoResult.getCode() == 200 && vehicleInfoResult.getData() != null) {
                var vehicleInfo = vehicleInfoResult.getData();
                vo.setVehicleNo(vehicleInfo.getLicensePlate());
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
        wrapper.ge(Trip::getActualStartTime, start.atStartOfDay())
               .le(Trip::getActualStartTime, end.atTime(23, 59, 59));
        
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
        
        // 统计各项数据
        int tripCount = trips.size();
        int completedCount = 0;
        int cancelledCount = 0;
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalDuration = BigDecimal.ZERO;
        
        for (Trip trip : trips) {
            // 统计完成和取消的行程
            if (trip.getStatus() != null && TripStatusEnum.COMPLETED.getCode() == trip.getStatus()) {
                completedCount++;
            } else if (trip.getStatus() != null && TripStatusEnum.CANCELLED.getCode() == trip.getStatus()) {
                cancelledCount++;
            }
            
            // 累加距离
            if (trip.getEstimatedMileage() != null) {
                totalDistance = totalDistance.add(trip.getEstimatedMileage());
            }
            
            // 累加时长
            if (trip.getActualStartTime() != null && trip.getActualEndTime() != null) {
                long minutes = Duration.between(trip.getActualStartTime(), trip.getActualEndTime()).toMinutes();
                totalDuration = totalDuration.add(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        
        // 计算平均速度
        BigDecimal averageSpeed = BigDecimal.ZERO;
        if (totalDuration.compareTo(BigDecimal.ZERO) > 0) {
            averageSpeed = totalDistance.divide(totalDuration, 2, BigDecimal.ROUND_HALF_UP);
        }
        
        dto.setTripCount(tripCount);
        dto.setTotalDistance(totalDistance);
        dto.setTotalDuration(totalDuration);
        dto.setCompletedTripCount(completedCount);
        dto.setCancelledTripCount(cancelledCount);
        dto.setAverageSpeed(averageSpeed);
        dto.setFuelConsumption(BigDecimal.ZERO); // 暂时设为 0
        dto.setCargoWeight(BigDecimal.ZERO); // 暂时设为 0
        
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
        // 调用 TripTrackService 获取轨迹点
        return tripTrackService.getByTripId(tripId);
    }
}