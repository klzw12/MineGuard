package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.VehicleFaultDTO;
import com.klzw.service.vehicle.entity.VehicleFault;

import java.util.List;

public interface VehicleFaultService extends IService<VehicleFault> {
    
    /**
     * 报告车辆故障
     * @param faultDTO 故障信息DTO
     * @return 故障信息
     */
    VehicleFault reportFault(VehicleFaultDTO faultDTO);
    
    /**
     * 处理车辆故障
     * @param id 故障ID
     * @param repairmanId 维修员ID
     * @param repairContent 维修内容
     * @param repairCost 维修费用
     * @return 故障信息
     */
    VehicleFault handleFault(Long id, Long repairmanId, String repairContent, java.math.BigDecimal repairCost);
    
    /**
     * 获取车辆故障记录
     * @param vehicleId 车辆ID
     * @param status 故障状态
     * @param page 页码
     * @param size 每页大小
     * @return 故障记录列表
     */
    List<VehicleFault> getFaultRecords(Long vehicleId, Integer status, int page, int size);
    
}
