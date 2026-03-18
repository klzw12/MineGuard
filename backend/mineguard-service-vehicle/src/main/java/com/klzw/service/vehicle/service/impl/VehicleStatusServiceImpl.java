package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.vehicle.client.TripServiceClient;
import com.klzw.service.vehicle.dto.VehicleStatusReportDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.enums.VehicleSpecialStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.mapper.VehicleStatusMapper;
import com.klzw.service.vehicle.service.VehicleStatusService;
import com.klzw.service.vehicle.service.VehicleStatusPushService;
import com.klzw.service.vehicle.vo.VehicleStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 车辆状态服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusServiceImpl extends ServiceImpl<VehicleStatusMapper, VehicleStatus> implements VehicleStatusService {
    
    private final VehicleMapper vehicleMapper;
    private final VehicleStatusMapper vehicleStatusMapper;
    private final VehicleStatusPushService vehicleStatusPushService;
    private final RedisCacheService redisCacheService;
    private final TripServiceClient tripServiceClient;
    private final com.klzw.service.vehicle.service.VehicleMaintenanceService vehicleMaintenanceService;

    @Override
    public VehicleStatusVO getRealTimeStatus(Long vehicleId) {
        log.info("获取车辆实时状态: vehicleId={}", vehicleId);
        
        // 1. 检查维修状态
        com.klzw.service.vehicle.entity.VehicleMaintenance latestMaintenance = vehicleMaintenanceService.getNextMaintenance(vehicleId);
        if (latestMaintenance != null && latestMaintenance.getNextMaintenanceDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!today.isBefore(latestMaintenance.getNextMaintenanceDate())) {
                VehicleStatusVO statusVO = new VehicleStatusVO();
                statusVO.setVehicleId(vehicleId.toString());
                statusVO.setStatus(4); // 维修中
                return statusVO;
            }
        }
        
        // 2. 通过HTTP Exchange查询最近行程状态
        try {
            var tripResponse = tripServiceClient.getLatestTrip(vehicleId).block();
            if (tripResponse != null && tripResponse.getCode() == 200 && tripResponse.getData() != null) {
                var tripData = tripResponse.getData();
                String tripStatus = tripData.getStatus();
                
                // 3. 根据行程状态判断车辆状态
                if ("IN_PROGRESS".equals(tripStatus)) {
                    // 4. 从Redis获取实时位置信息
                    String redisKey = "vehicle:status:" + vehicleId;
                    VehicleStatusVO statusVO = redisCacheService.get(redisKey);
                    if (statusVO != null) {
                        statusVO.setStatus(2); // 行驶中
                        return statusVO;
                    }
                } else if ("COMPLETED".equals(tripStatus)) {
                    // 行程已完成，车辆离线
                    VehicleStatusVO statusVO = new VehicleStatusVO();
                    statusVO.setVehicleId(vehicleId.toString());
                    statusVO.setStatus(0); // 离线
                    return statusVO;
                }
            }
        } catch (Exception e) {
            log.warn("查询行程服务失败: vehicleId={}", vehicleId, e);
        }
        
        // 默认返回离线状态
        VehicleStatusVO statusVO = new VehicleStatusVO();
        statusVO.setVehicleId(vehicleId.toString());
        statusVO.setStatus(0); // 离线
        return statusVO;
    }

    @Override
    public VehicleStatus updateStatus(Long vehicleId, VehicleStatus status) {
        log.info("更新车辆状态: vehicleId={}, status={}", vehicleId, status);
        
        // 1. 保存状态到数据库
        status.setVehicleId(vehicleId);
        save(status);
        
        // 2. 更新Redis缓存
        String redisKey = "vehicle:status:" + vehicleId;
        VehicleStatusVO statusVO = convertToVO(status);
        redisCacheService.set(redisKey, statusVO, 30, TimeUnit.MINUTES);
        
        // 3. 推送状态变更
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
        
        return status;
    }

    @Override
    public List<VehicleStatusVO> getStatusHistory(Long vehicleId, int page, int size) {
        log.info("获取车辆状态历史: vehicleId={}, page={}, size={}", vehicleId, page, size);
        
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
        log.info("收到车辆状态上报: vehicleId={}, tripId={}, specialStatus={}", 
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
            log.warn("车辆特殊状态: vehicleId={}, status={}", vehicleId, 
                    VehicleSpecialStatusEnum.getByCode(reportDTO.getSpecialStatus()).getDesc());
            
            if (reportDTO.getTripId() != null) {
                try {
                    tripServiceClient.pauseTrip(reportDTO.getTripId()).block();
                    log.info("行程已暂停: tripId={}", reportDTO.getTripId());
                } catch (Exception e) {
                    log.error("暂停行程失败: tripId={}", reportDTO.getTripId(), e);
                }
            }
        } else {
            statusVO.setStatus(2);
        }
        
        redisCacheService.set(redisKey, statusVO, 30, TimeUnit.MINUTES);
        log.debug("车辆状态已存入Redis: vehicleId={}", vehicleId);
        
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
        log.debug("车辆状态已存入数据库: vehicleId={}", vehicleId);
        
        vehicleStatusPushService.pushStatusChange(vehicleId, statusVO);
    }
}
