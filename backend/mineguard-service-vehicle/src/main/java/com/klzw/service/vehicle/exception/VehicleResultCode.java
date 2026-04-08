package com.klzw.service.vehicle.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆模块错误码枚举
 * <p>
 * 错误码范围：2000-2099
 */
@Getter
@AllArgsConstructor
public enum VehicleResultCode {

    VEHICLE_NOT_FOUND(2001, "车辆不存在"),
    VEHICLE_NO_EXISTS(2002, "车牌号已存在"),
    VEHICLE_STATUS_NOT_ALLOWED(2003, "车辆状态不允许该操作"),
    FAULT_NOT_FOUND(2004, "故障记录不存在"),
    INSURANCE_NOT_FOUND(2005, "保险记录不存在"),
    MAINTENANCE_NOT_FOUND(2006, "保养记录不存在"),
    REFUELING_NOT_FOUND(2007, "加油记录不存在"),
    PARAMETER_ERROR(2008, "参数错误"),
    OPERATION_FAILED(2009, "操作失败"),
    VEHICLE_NO_MISMATCH(2010, "车牌号与行驶证识别结果不一致");

    private final Integer code;
    private final String message;
}
