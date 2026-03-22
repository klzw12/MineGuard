package com.klzw.service.vehicle.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆模块错误码枚举
 * <p>
 * 错误码范围：1000-1099
 */
@Getter
@AllArgsConstructor
public enum VehicleResultCode {

    /**
     * 车辆不存在
     */
    VEHICLE_NOT_FOUND(1001, "车辆不存在"),

    /**
     * 车牌号已存在
     */
    VEHICLE_NO_EXISTS(1002, "车牌号已存在"),

    /**
     * 车辆状态不允许该操作
     */
    VEHICLE_STATUS_NOT_ALLOWED(1003, "车辆状态不允许该操作"),

    /**
     * 故障记录不存在
     */
    FAULT_NOT_FOUND(1004, "故障记录不存在"),

    /**
     * 保险记录不存在
     */
    INSURANCE_NOT_FOUND(1005, "保险记录不存在"),

    /**
     * 保养记录不存在
     */
    MAINTENANCE_NOT_FOUND(1006, "保养记录不存在"),

    /**
     * 加油记录不存在
     */
    REFUELING_NOT_FOUND(1007, "加油记录不存在"),

    /**
     * 参数错误
     */
    PARAMETER_ERROR(1008, "参数错误"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(1009, "操作失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;
}
