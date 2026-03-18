package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.VehicleMaintenanceDTO;
import com.klzw.service.vehicle.entity.VehicleMaintenance;

import java.util.List;

public interface VehicleMaintenanceService extends IService<VehicleMaintenance> {
    
    /**
     * 添加车辆保养记录
     * @param maintenanceDTO 保养记录DTO
     * @return 保养记录
     */
    VehicleMaintenance addMaintenanceRecord(VehicleMaintenanceDTO maintenanceDTO);
    
    /**
     * 获取车辆保养记录
     * @param vehicleId 车辆ID
     * @param page 页码
     * @param size 每页大小
     * @return 保养记录列表
     */
    List<VehicleMaintenance> getMaintenanceRecords(Long vehicleId, int page, int size);
    
    /**
     * 获取车辆下次保养信息
     * @param vehicleId 车辆ID
     * @return 保养记录
     */
    VehicleMaintenance getNextMaintenance(Long vehicleId);
    
}
