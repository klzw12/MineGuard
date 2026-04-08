package com.klzw.service.trip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行程模块错误码枚举
 * <p>
 * 错误码范围：2300-2399
 */
@Getter
@AllArgsConstructor
public enum TripResultCode {

    TRIP_NOT_FOUND(2300, "行程不存在"),
    TRIP_ALREADY_STARTED(2301, "行程已开始"),
    TRIP_ALREADY_ENDED(2302, "行程已结束"),
    TRIP_NOT_STARTED(2303, "行程未开始"),
    TRIP_STATUS_ERROR(2304, "行程状态错误"),
    
    ROUTE_NOT_FOUND(2310, "路线不存在"),
    ROUTE_NAME_EXISTS(2311, "路线名称已存在"),
    
    DISPATCH_PLAN_NOT_FOUND(2320, "调度计划不存在"),
    DISPATCH_PLAN_EXECUTED(2321, "调度计划已执行"),
    
    TRACK_UPLOAD_FAILED(2330, "轨迹上传失败"),
    TRACK_NOT_FOUND(2331, "轨迹记录不存在"),
    
    VEHICLE_NOT_AVAILABLE(2340, "车辆不可用"),
    DRIVER_NOT_AVAILABLE(2341, "司机不可用");

    private final int code;
    private final String message;
}
