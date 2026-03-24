package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.service.vehicle.dto.VehicleMaintenanceDTO;
import com.klzw.service.vehicle.entity.VehicleMaintenance;
import com.klzw.service.vehicle.mapper.VehicleMaintenanceMapper;
import com.klzw.service.vehicle.service.VehicleMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class VehicleMaintenanceServiceImpl extends ServiceImpl<VehicleMaintenanceMapper, VehicleMaintenance> implements VehicleMaintenanceService {
    
    @Resource
    private VehicleMaintenanceMapper vehicleMaintenanceMapper;
    
    @Override
    public VehicleMaintenance addMaintenanceRecord(VehicleMaintenanceDTO maintenanceDTO) {
        log.info("添加车辆保养记录: vehicleId={}, maintenanceDate={}", maintenanceDTO.getVehicleId(), maintenanceDTO.getMaintenanceDate());
        // 转换DTO为实体类
        VehicleMaintenance maintenance = new VehicleMaintenance();
        maintenance.setVehicleId(maintenanceDTO.getVehicleId());
        maintenance.setMaintenanceType(maintenanceDTO.getMaintenanceType());
        maintenance.setMaintenanceDate(maintenanceDTO.getMaintenanceDate().atStartOfDay());
        maintenance.setMaintenanceContent(maintenanceDTO.getMaintenanceContent());
        maintenance.setMaintenanceCost(maintenanceDTO.getMaintenanceCost());
        maintenance.setNextMaintenanceDate(maintenanceDTO.getNextMaintenanceDate());
        if (maintenanceDTO.getMileage() != null) {
            maintenance.setMileage(maintenanceDTO.getMileage().intValue());
        }
        maintenance.setRemark(maintenanceDTO.getRemark());
        save(maintenance);
        return maintenance;
    }
    
    @Override
    public List<VehicleMaintenance> getMaintenanceRecords(Long vehicleId, int page, int size) {
        log.info("获取车辆保养记录: vehicleId={}, page={}, size={}", vehicleId, page, size);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleMaintenance> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleId, vehicleId)
               .orderByDesc(VehicleMaintenance::getMaintenanceDate);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VehicleMaintenance> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public VehicleMaintenance getNextMaintenance(Long vehicleId) {
        log.info("获取车辆下次保养信息: vehicleId={}", vehicleId);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VehicleMaintenance> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleId, vehicleId)
               .isNotNull(VehicleMaintenance::getNextMaintenanceDate)
               .orderByDesc(VehicleMaintenance::getNextMaintenanceDate)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }
    
}
