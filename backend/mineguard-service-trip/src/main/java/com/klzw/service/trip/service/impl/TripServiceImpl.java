package com.klzw.service.trip.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.util.GeoUtils;
import com.klzw.service.trip.client.UserServiceClient;
import com.klzw.service.trip.client.VehicleServiceClient;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.processor.TripStatusProcessor;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.service.TripTrackService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip> implements TripService {

    private final UserServiceClient userServiceClient;
    private final VehicleServiceClient vehicleServiceClient;
    private final TripStatusProcessor tripStatusProcessor;
    private final TripTrackService tripTrackService;

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
            trip.setEstimatedDuration((int) (distance / 60 * 60)); // 假设平均速度60km/h
        } catch (Exception e) {
            log.warn("计算预计里程和时长失败", e);
        }
        
        getBaseMapper().insert(trip);
        log.info("创建行程成功，行程ID：{}，行程编号：{}", trip.getId(), trip.getTripNo());
        
        // 处理状态变化
        tripStatusProcessor.processStatusChange(trip, oldStatus, newStatus);
        
        return trip.getId();
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
        log.info("开始行程成功，行程ID：{}", id);
        
        // 处理状态变化
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
        
        // 批量写入轨迹点到MySQL
        try {
            List<com.klzw.service.trip.dto.TripTrackDTO> tracks = tripTrackService.getTracksFromRedis(id);
            if (!tracks.isEmpty()) {
                tripTrackService.batchSaveTracks(tracks);
                // 删除Redis中的轨迹数据
                tripTrackService.deleteTracksFromRedis(id);
                log.info("轨迹点批量写入MySQL成功，行程ID：{}，数量：{}", id, tracks.size());
            }
        } catch (Exception e) {
            log.error("批量写入轨迹点失败，行程ID：{}", id, e);
            // 不影响行程结束流程
        }
        
        // 处理状态变化
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
        response.setCode(200);
        response.setMessage("success");
        
        if (trip != null) {
            TripResponse.TripData data = new TripResponse.TripData();
            data.setId(trip.getId());
            data.setVehicleId(trip.getVehicleId());
            data.setDriverId(trip.getDriverId());
            data.setStatus(TripStatusEnum.getByCode(trip.getStatus()).name());
            response.setData(data);
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
        // 验证车辆是否存在
        boolean vehicleExists = vehicleServiceClient.existsById(vehicleId)
                .block();
        if (!vehicleExists) {
            throw new TripException(TripResultCode.VEHICLE_NOT_AVAILABLE, "车辆不存在或不可用");
        }
        
        // 验证司机是否存在
        boolean driverExists = userServiceClient.existsById(driverId)
                .block();
        if (!driverExists) {
            throw new TripException(TripResultCode.DRIVER_NOT_AVAILABLE, "司机不存在或不可用");
        }
    }

    private String generateTripNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = IdUtil.fastSimpleUUID().substring(0, 6);
        return "TRIP" + timestamp + random;
    }

    private TripVO convertToVO(Trip trip) {
        TripVO vo = new TripVO();
        BeanUtils.copyProperties(trip, vo);
        
        // 设置车辆信息
        try {
            var vehicleInfo = vehicleServiceClient.getVehicleById(trip.getVehicleId())
                    .block();
            if (vehicleInfo != null) {
                vo.setVehicleNo(vehicleInfo.getLicensePlate());
            }
        } catch (Exception e) {
            log.warn("获取车辆信息失败，vehicleId={}", trip.getVehicleId(), e);
        }
        
        // 设置司机信息
        try {
            var userInfo = userServiceClient.getUserById(trip.getDriverId())
                    .block();
            if (userInfo != null) {
                vo.setDriverName(userInfo.getRealName());
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

    private BigDecimal calculateActualDistance(Long tripId) {
        List<TripTrackVO> tracks = tripTrackService.getByTripId(tripId);
        if (tracks == null || tracks.size() < 2) {
            return null;
        }

        double totalDistance = 0.0;
        for (int i = 1; i < tracks.size(); i++) {
            TripTrackVO prev = tracks.get(i - 1);
            TripTrackVO curr = tracks.get(i);
            
            if (prev.getLongitude() != null && prev.getLatitude() != null
                && curr.getLongitude() != null && curr.getLatitude() != null) {
                GeoPoint prevPoint = new GeoPoint(prev.getLongitude(), prev.getLatitude());
                GeoPoint currPoint = new GeoPoint(curr.getLongitude(), curr.getLatitude());
                totalDistance += GeoUtils.calculateDistance(prevPoint, currPoint);
            }
        }

        return BigDecimal.valueOf(totalDistance / 1000.0);
    }
}