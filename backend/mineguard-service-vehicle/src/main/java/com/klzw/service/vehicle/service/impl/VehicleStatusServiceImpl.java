package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.service.vehicle.dto.VehicleStatusReportDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.enums.VehicleSpecialStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.mapper.VehicleStatusMapper;
import com.klzw.service.vehicle.service.VehicleStatusService;
import com.klzw.service.vehicle.service.VehicleStatusPushService;
import com.klzw.service.vehicle.service.FatigueWarningService;
import com.klzw.service.vehicle.vo.VehicleStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusServiceImpl extends ServiceImpl<VehicleStatusMapper, VehicleStatus> implements VehicleStatusService {
    
    private final VehicleMapper vehicleMapper;
    private final VehicleStatusMapper vehicleStatusMapper;
    private final VehicleStatusPushService vehicleStatusPushService;
    private final RedisCacheService redisCacheService;
    private final TripClient tripClient;
    private final com.klzw.service.vehicle.service.VehicleMaintenanceService vehicleMaintenanceService;
    private final FatigueWarningService fatigueWarningService;

    private static final int HEARTBEAT_INTERVAL_SECONDS = 30;
    private static final int FATIGUE_THRESHOLD_MINUTES = 240;
    private static final int FATIGUE_REST_MINUTES = 30;

    @Override
    public VehicleStatusVO getRealTimeStatus(Long vehicleId) {
        log.info("获取车辆实时状态：vehicleId={}", vehicleId);
        
        // 1. 检查维修状态
        com.klzw.service.vehicle.entity.VehicleMaintenance latestMaintenance = vehicleMaintenanceService.getNextMaintenance(vehicleId);
        if (latestMaintenance != null && latestMaintenance.getNextMaintenanceDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!today.isBefore(latestMaintenance.getNextMaintenanceDate())) {
                log.info("车辆在维修中：vehicleId={}, 下次保养日期={}", vehicleId, latestMaintenance.getNextMaintenanceDate());
                VehicleStatusVO statusVO = new VehicleStatusVO();
                statusVO.setVehicleId(vehicleId.toString());
                statusVO.setStatus(4); // 维修中
                return statusVO;
            }
        }
        
        // 2. 从 Redis 查询车辆是否有 tripId（实时状态判断）
        String tripKey = "trip:track:vehicle:" + vehicleId;
        Long currentTripId = redisCacheService.get(tripKey);
        
        if (currentTripId != null) {
            // 有 tripId = 运行中
            log.info("车辆运行中：vehicleId={}, tripId={}", vehicleId, currentTripId);
            
            // 从 Redis 获取实时位置信息
            String statusKey = "vehicle:status:" + vehicleId;
            VehicleStatusVO statusVO = redisCacheService.get(statusKey);
            if (statusVO == null) {
                statusVO = new VehicleStatusVO();
                statusVO.setVehicleId(vehicleId.toString());
            }
            statusVO.setStatus(2); // 行驶中
            return statusVO;
        }
        
        // 3. 无 tripId = 空闲/离线
        log.info("车辆空闲/离线：vehicleId={}", vehicleId);
        VehicleStatusVO statusVO = new VehicleStatusVO();
        statusVO.setVehicleId(vehicleId.toString());
        statusVO.setStatus(0); // 离线
        return statusVO;
    }

    @Override
    public VehicleStatus updateStatus(Long vehicleId, VehicleStatus status) {
        log.info("更新车辆状态：vehicleId={}, status={}", vehicleId, status);
        
        // 1. 保存状态到数据库
        status.setVehicleId(vehicleId);
        save(status);
        
        // 2. 更新 Redis 缓存
        String redisKey = "vehicle:status:" + vehicleId;
        VehicleStatusVO statusVO = convertToVO(status);
        redisCacheService.set(redisKey, statusVO, 30, TimeUnit.MINUTES);
        
        // 3. 推送状态变更
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
        
        return status;
    }

    @Override
    public List<VehicleStatusVO> getStatusHistory(Long vehicleId, int page, int size) {
        log.info("获取车辆状态历史：vehicleId={}, page={}, size={}", vehicleId, page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleStatus> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleStatus> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleStatus::getVehicleId, vehicleId)
               .orderByDesc(VehicleStatus::getCreateTime);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleStatus> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private VehicleStatusVO convertToVO(VehicleStatus status) {
        VehicleStatusVO vo = new VehicleStatusVO();
        vo.setVehicleId(status.getVehicleId().toString());
        vo.setLongitude(status.getLongitude());
        vo.setLatitude(status.getLatitude());
        vo.setSpeed(status.getSpeed() != null ? status.getSpeed().doubleValue() : null);
        vo.setDirection(status.getDirection() != null ? status.getDirection().doubleValue() : null);
        vo.setMileage(status.getMileage() != null ? status.getMileage().doubleValue() : null);
        vo.setFuelLevel(status.getFuelLevel());
        vo.setStatus(status.getStatus());
        vo.setCreateTime(status.getCreateTime());
        
        // 设置车辆信息
        Vehicle vehicle = vehicleMapper.selectById(status.getVehicleId());
        if (vehicle != null) {
            vo.setVehicleNo(vehicle.getVehicleNo());
        }
        
        return vo;
    }

    @Override
    public void reportStatus(VehicleStatusReportDTO reportDTO) {
        log.info("收到车辆状态上报：vehicleId={}, tripId={}, specialStatus={}", 
                reportDTO.getVehicleId(), reportDTO.getTripId(), reportDTO.getSpecialStatus());
        
        Long vehicleId = reportDTO.getVehicleId();
        
        if (reportDTO.getReportTime() == null) {
            reportDTO.setReportTime(LocalDateTime.now());
        }
        
        String redisKey = "vehicle:status:" + vehicleId;
        
        VehicleStatusVO statusVO = new VehicleStatusVO();
        statusVO.setVehicleId(vehicleId.toString());
        statusVO.setLongitude(reportDTO.getLongitude());
        statusVO.setLatitude(reportDTO.getLatitude());
        statusVO.setSpeed(reportDTO.getSpeed());
        statusVO.setDirection(reportDTO.getDirection());
        statusVO.setMileage(reportDTO.getMileage());
        statusVO.setFuelLevel(reportDTO.getFuelLevel());
        statusVO.setReportTime(reportDTO.getReportTime());
        
        if (reportDTO.getSpecialStatus() != null && reportDTO.getSpecialStatus() != VehicleSpecialStatusEnum.NORMAL.getCode()) {
            statusVO.setStatus(3);
            log.warn("车辆特殊状态：vehicleId={}, status={}", vehicleId, 
                    VehicleSpecialStatusEnum.getByCode(reportDTO.getSpecialStatus()).getDesc());
            
            if (reportDTO.getTripId() != null) {
                try {
                    tripClient.pauseTrip(reportDTO.getTripId()).block();
                    log.info("行程已暂停：tripId={}", reportDTO.getTripId());
                } catch (Exception e) {
                    log.error("暂停行程失败：tripId={}", reportDTO.getTripId(), e);
                }
            }
        } else {
            statusVO.setStatus(2);
            
            if (reportDTO.getSpeed() != null && reportDTO.getSpeed() > 0) {
                checkFatigueDriving(vehicleId, reportDTO.getTripId());
            } else {
                resetFatigueDrivingTime(vehicleId);
            }
        }
        
        redisCacheService.set(redisKey, statusVO, 30, TimeUnit.MINUTES);
        log.debug("车辆状态已存入 Redis: vehicleId={}", vehicleId);
        
        VehicleStatus vehicleStatus = new VehicleStatus();
        vehicleStatus.setVehicleId(vehicleId);
        vehicleStatus.setTripId(reportDTO.getTripId());
        vehicleStatus.setLongitude(reportDTO.getLongitude());
        vehicleStatus.setLatitude(reportDTO.getLatitude());
        vehicleStatus.setSpeed(reportDTO.getSpeed());
        vehicleStatus.setDirection(reportDTO.getDirection());
        vehicleStatus.setMileage(reportDTO.getMileage());
        vehicleStatus.setFuelLevel(reportDTO.getFuelLevel());
        vehicleStatus.setStatus(statusVO.getStatus());
        vehicleStatus.setReportTime(reportDTO.getReportTime());
        save(vehicleStatus);
        log.debug("车辆状态已存入数据库：vehicleId={}", vehicleId);
        
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
    }
    
    private void checkFatigueDriving(Long vehicleId, Long tripId) {
        String fatigueRestKey = "vehicle:fatigue:rest:" + vehicleId;
        if (redisCacheService.exists(fatigueRestKey)) {
            log.warn("司机处于疲劳驾驶休息状态：vehicleId={}", vehicleId);
            return;
        }
        
        String fatigueKey = "vehicle:fatigue:" + vehicleId;
        Integer currentMinutes = redisCacheService.get(fatigueKey);
        if (currentMinutes == null) {
            currentMinutes = 0;
        }
        
        currentMinutes += 1;
        redisCacheService.set(fatigueKey, currentMinutes, 8, TimeUnit.HOURS);
        
        log.debug("疲劳驾驶时间累计：vehicleId={}, minutes={}", vehicleId, currentMinutes);
        
        if (currentMinutes >= FATIGUE_THRESHOLD_MINUTES) {
            log.warn("疲劳驾驶预警：vehicleId={}, 连续驾驶时间={}分钟", vehicleId, currentMinutes);
            fatigueWarningService.sendFatigueWarning(vehicleId, tripId, currentMinutes);
            
            redisCacheService.set(fatigueRestKey, "REST_REQUIRED", FATIGUE_REST_MINUTES, TimeUnit.MINUTES);
            log.info("已标记司机疲劳驾驶休息状态：vehicleId={}, 需休息{}分钟", vehicleId, FATIGUE_REST_MINUTES);
        }
    }
    
    private void resetFatigueDrivingTime(Long vehicleId) {
        String fatigueKey = "vehicle:fatigue:" + vehicleId;
        String fatigueRestKey = "vehicle:fatigue:rest:" + vehicleId;
        
        redisCacheService.delete(fatigueKey);
        redisCacheService.delete(fatigueRestKey);
        
        log.debug("疲劳驾驶时间重置：vehicleId={}", vehicleId);
    }
}
