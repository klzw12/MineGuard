package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.VehicleInsuranceDTO;
import com.klzw.service.vehicle.entity.VehicleInsurance;

import java.util.List;

public interface VehicleInsuranceService extends IService<VehicleInsurance> {
    
    /**
     * 添加车辆保险信息
     * @param insuranceDTO 保险信息DTO
     * @return 保险信息
     */
    VehicleInsurance addInsurance(VehicleInsuranceDTO insuranceDTO);
    
    /**
     * 获取所有保险记录
     * @param page 页码
     * @param size 每页大小
     * @return 保险记录列表
     */
    List<VehicleInsurance> getAllInsuranceRecords(int page, int size);
    
    /**
     * 获取车辆保险信息
     * @param vehicleId 车辆ID
     * @return 保险信息列表
     */
    List<VehicleInsurance> getVehicleInsurance(Long vehicleId);
    
    /**
     * 获取车辆当前有效的保险信息
     * @param vehicleId 车辆ID
     * @return 保险信息
     */
    VehicleInsurance getCurrentInsurance(Long vehicleId);
    
}
