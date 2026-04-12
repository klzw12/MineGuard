package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.common.core.client.TripClient;
import com.klzw.service.vehicle.dto.VehicleStatusReportDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.enums.VehicleSpecialStatusEnum;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.mapper.VehicleStatusMapper;
import com.klzw.service.vehicle.service.VehicleStatusService;
import com.klzw.service.vehicle.service.VehicleStatusPushService;
import com.klzw.service.vehicle.vo.VehicleStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusServiceImpl extends ServiceImpl<VehicleStatusMapper, VehicleStatus> implements VehicleStatusService {
    
    private final VehicleMapper vehicleMapper;
    private final VehicleStatusPushService vehicleStatusPushService;
    private final RedisCacheService redisCacheService;
    private final TripClient tripClient;
    private final com.klzw.service.vehicle.service.VehicleMaintenanceService vehicleMaintenanceService;

    @Override
    public VehicleStatusVO getRealTimeStatus(Long vehicleId) {
        log.info("获取车辆实时状态：vehicleId={}", vehicleId);
        
        com.klzw.service.vehicle.entity.VehicleMaintenance latestMaintenance = vehicleMaintenanceService.getNextMaintenance(vehicleId);
        if (latestMaintenance != null && latestMaintenance.getNextMaintenanceDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!today.isBefore(latestMaintenance.getNextMaintenanceDate())) {
                log.info("车辆在维修中：vehicleId={}, 下次保养日期={}", vehicleId, latestMaintenance.getNextMaintenanceDate());
                VehicleStatusVO statusVO = new VehicleStatusVO();
                statusVO.setVehicleId(vehicleId.toString());
                statusVO.setStatus(VehicleStatusEnum.MAINTENANCE.getCode());
                return statusVO;
            }
        }
        
        String tripKey = "trip:track:vehicle:" + vehicleId;
        Long currentTripId = redisCacheService.get(tripKey);
        
        if (currentTripId != null) {
            log.info("车辆运行中：vehicleId={}, tripId={}", vehicleId, currentTripId);
            
            String statusKey = "vehicle:status:" + vehicleId;
            VehicleStatusVO statusVO = redisCacheService.get(statusKey);
            if (statusVO == null) {
                statusVO = new VehicleStatusVO();
                statusVO.setVehicleId(vehicleId.toString());
            }
            statusVO.setStatus(VehicleStatusEnum.RUNNING.getCode());
            return statusVO;
        }
        
        log.info("车辆空闲/离线：vehicleId={}", vehicleId);
        VehicleStatusVO statusVO = new VehicleStatusVO();
        statusVO.setVehicleId(vehicleId.toString());
        statusVO.setStatus(VehicleStatusEnum.IDLE.getCode());
        return statusVO;
    }

    @Override
    public VehicleStatus updateStatus(Long vehicleId, VehicleStatus status) {
        Integer newStatus = status.getStatus();
        
        log.info("车辆状态变更：vehicleId={}, newStatus={}", vehicleId, newStatus);
        
        status.setVehicleId(vehicleId);
        save(status);
        
        String redisKey = "vehicle:status:" + vehicleId;
        VehicleStatusVO statusVO = convertToVO(status);
        int expireMinutes = newStatus != null && newStatus.equals(VehicleStatusEnum.RUNNING.getCode()) 
            ? 5 : 60;
        redisCacheService.set(redisKey, statusVO, expireMinutes, TimeUnit.MINUTES);
        
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
        
        log.info("车辆状态更新完成：vehicleId={}, status={}, Redis缓存过期={}分钟", vehicleId, newStatus, expireMinutes);
        
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
        vo.setMileage(status.getMileage() != null ? status.getMileage().doubleValue() : null);
        vo.setFuelLevel(status.getFuelLevel());
        vo.setStatus(status.getStatus());
        vo.setCreateTime(status.getCreateTime());
        vo.setReportTime(status.getReportTime());
        
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
        statusVO.setMileage(reportDTO.getMileage());
        statusVO.setFuelLevel(reportDTO.getFuelLevel());
        statusVO.setReportTime(reportDTO.getReportTime());
        
        if (reportDTO.getSpecialStatus() != null && reportDTO.getSpecialStatus() != VehicleSpecialStatusEnum.NORMAL.getCode()) {
            statusVO.setStatus(VehicleStatusEnum.FAULT.getCode());
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
            statusVO.setStatus(VehicleStatusEnum.RUNNING.getCode());
        }
        
        redisCacheService.set(redisKey, statusVO, 30, TimeUnit.MINUTES);
        log.debug("车辆状态已存入 Redis: vehicleId={}", vehicleId);
        
        VehicleStatus vehicleStatus = new VehicleStatus();
        vehicleStatus.setVehicleId(vehicleId);
        vehicleStatus.setTripId(reportDTO.getTripId());
        vehicleStatus.setLongitude(reportDTO.getLongitude());
        vehicleStatus.setLatitude(reportDTO.getLatitude());
        vehicleStatus.setMileage(reportDTO.getMileage());
        vehicleStatus.setFuelLevel(reportDTO.getFuelLevel());
        vehicleStatus.setStatus(statusVO.getStatus());
        vehicleStatus.setReportTime(reportDTO.getReportTime());
        save(vehicleStatus);
        log.debug("车辆状态已存入数据库：vehicleId={}", vehicleId);
        
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
    }
    
    @Override
    public VehicleStatus getByVehicleId(Long vehicleId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleStatus> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleStatus::getVehicleId, vehicleId)
               .orderByDesc(VehicleStatus::getCreateTime)
               .last("LIMIT 1");
        return getOne(wrapper);
    }
}
