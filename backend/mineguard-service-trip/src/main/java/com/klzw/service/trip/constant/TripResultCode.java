package com.klzw.service.trip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TripResultCode {

    TRIP_NOT_FOUND(2000, "行程不存在"),
    TRIP_ALREADY_STARTED(2001, "行程已开始"),
    TRIP_ALREADY_ENDED(2002, "行程已结束"),
    TRIP_NOT_STARTED(2003, "行程未开始"),
    TRIP_STATUS_ERROR(2004, "行程状态错误"),
    
    ROUTE_NOT_FOUND(2010, "路线不存在"),
    ROUTE_NAME_EXISTS(2011, "路线名称已存在"),
    
    DISPATCH_PLAN_NOT_FOUND(2020, "调度计划不存在"),
    DISPATCH_PLAN_EXECUTED(2021, "调度计划已执行"),
    
    TRACK_UPLOAD_FAILED(2030, "轨迹上传失败"),
    TRACK_NOT_FOUND(2031, "轨迹记录不存在"),
    
    VEHICLE_NOT_AVAILABLE(2040, "车辆不可用"),
    DRIVER_NOT_AVAILABLE(2041, "司机不可用");

    private final int code;
    private final String message;
}
