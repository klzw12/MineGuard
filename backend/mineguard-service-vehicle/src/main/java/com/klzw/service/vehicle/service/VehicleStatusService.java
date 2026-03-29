package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.VehicleStatusReportDTO;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.vo.VehicleStatusVO;

import java.util.List;

/**
 * 车辆状态服务接口
 */
public interface VehicleStatusService extends IService<VehicleStatus> {
    
    /**
     * 获取车辆实时状态
     * @param vehicleId 车辆ID
     * @return 车辆状态
     */
    VehicleStatusVO getRealTimeStatus(Long vehicleId);
    
    /**
     * 更新车辆状态
     * @param vehicleId 车辆ID
     * @param status 状态信息
     * @return 车辆状态
     */
    VehicleStatus updateStatus(Long vehicleId, VehicleStatus status);
    
    /**
     * 获取车辆状态历史
     * @param vehicleId 车辆ID
     * @param page 页码
     * @param size 每页大小
     * @return 状态历史列表
     */
    List<VehicleStatusVO> getStatusHistory(Long vehicleId, int page, int size);
    
    /**
     * 上报车辆状态（包括位置和特殊状态）
     * @param reportDTO 上报数据
     */
    void reportStatus(VehicleStatusReportDTO reportDTO);
    
    /**
     * 根据车辆ID获取状态记录
     * @param vehicleId 车辆ID
     * @return 车辆状态
     */
    VehicleStatus getByVehicleId(Long vehicleId);
}
