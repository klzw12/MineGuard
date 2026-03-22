package com.klzw.service.dispatch.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调度模块错误码枚举
 */
@Getter
@AllArgsConstructor
public enum DispatchResultCode {
    
    // 通用错误 1000-1099
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 调度计划错误 2000-2099
    PLAN_NOT_FOUND(2000, "调度计划不存在"),
    PLAN_STATUS_INVALID(2001, "调度计划状态无效"),
    PLAN_CANNOT_MODIFY(2002, "该计划不可修改"),
    PLAN_CANNOT_DELETE(2003, "该计划不可删除"),
    PLAN_CANNOT_EXECUTE(2004, "该计划不可执行"),
    
    // 调度任务错误 2100-2199
    TASK_NOT_FOUND(2100, "调度任务不存在"),
    TASK_STATUS_INVALID(2101, "调度任务状态无效"),
    TASK_CONFLICT(2102, "任务时间冲突"),
    TASK_CANNOT_ASSIGN(2103, "任务无法分配"),
    
    // 车辆相关错误 2200-2299
    VEHICLE_NOT_FOUND(2200, "车辆不存在"),
    VEHICLE_NOT_AVAILABLE(2201, "车辆不可用"),
    VEHICLE_CONFLICT(2202, "车辆时间冲突"),
    
    // 司机相关错误 2300-2399
    DRIVER_NOT_FOUND(2300, "司机不存在"),
    DRIVER_NOT_AVAILABLE(2301, "司机不可用"),
    DRIVER_CONFLICT(2302, "司机时间冲突"),
    DRIVER_SCORE_TOO_LOW(2303, "司机评分过低"),
    
    // 路线相关错误 2400-2499
    ROUTE_NOT_FOUND(2400, "路线模板不存在"),
    ROUTE_NAME_EXISTS(2401, "路线名称已存在"),
    ROUTE_BLOCKED(2402, "路线已堵塞"),
    
    // 动态调整错误 2500-2599
    ADJUSTMENT_FAILED(2500, "动态调整失败"),
    NO_AVAILABLE_DRIVER(2501, "无可用司机"),
    NO_AVAILABLE_VEHICLE(2502, "无可用车辆"),
    
    // 参数错误 2900-2999
    PARAM_ERROR(2900, "参数错误"),
    DATA_INVALID(2901, "数据无效");
    
    private final Integer code;
    private final String message;
}
