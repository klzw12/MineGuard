package com.klzw.service.vehicle.service;

import com.klzw.service.vehicle.vo.VehicleStatusVO;
import java.util.Map;

/**
 * 车辆状态推送服务接口
 */
public interface VehicleStatusPushService {

    /**
     * 推送车辆状态
     * @param vehicleId 车辆ID
     * @param status 状态信息
     */
    void pushVehicleStatus(Long vehicleId, Map<String, Object> status);

    /**
     * 推送车辆状态变更
     * @param vehicleId 车辆ID
     * @param statusVO 状态信息
     */
    void pushStatusChange(Long vehicleId, VehicleStatusVO statusVO);

    /**
     * 推送车辆告警
     * @param vehicleId 车辆ID
     * @param warningType 告警类型
     * @param warningMessage 告警消息
     */
    void pushVehicleWarning(Long vehicleId, String warningType, String warningMessage);
}
