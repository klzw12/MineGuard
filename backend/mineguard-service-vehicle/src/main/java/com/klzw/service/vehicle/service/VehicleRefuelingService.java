package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.VehicleRefuelingDTO;
import com.klzw.service.vehicle.entity.VehicleRefueling;

import java.util.List;

public interface VehicleRefuelingService extends IService<VehicleRefueling> {
    
    /**
     * 添加车辆加油记录
     * @param refuelingDTO 加油记录DTO
     * @return 加油记录
     */
    VehicleRefueling addRefuelingRecord(VehicleRefuelingDTO refuelingDTO);
    
    /**
     * 获取所有加油记录
     * @param page 页码
     * @param size 每页大小
     * @return 加油记录列表
     */
    List<VehicleRefueling> getAllRefuelingRecords(int page, int size);
    
    /**
     * 获取车辆加油记录
     * @param vehicleId 车辆ID
     * @param page 页码
     * @param size 每页大小
     * @return 加油记录列表
     */
    List<VehicleRefueling> getRefuelingRecords(Long vehicleId, int page, int size);
    
}
