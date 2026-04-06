package com.klzw.service.dispatch.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调度模块错误码枚举
 * <p>
 * 错误码范围：900-999
 * <p>
 * 错误码说明：
 * - 900: 调度通用错误
 * - 901-909: 调度相关错误
 * - 910-919: 任务相关错误
 * - 920-929: 路线相关错误
 * - 930-939: 车辆相关错误
 * - 940-949: 司机相关错误
 */
@Getter
@AllArgsConstructor
public enum DispatchResultCode {

    /**
     * 调度通用错误
     */
    DISPATCH_FAILED(900, "调度失败"),
    PARAMETER_ERROR(901, "参数错误"),

    /**
     * 调度相关错误
     */
    NO_AVAILABLE_DRIVER(902, "无可用司机"),
    NO_AVAILABLE_VEHICLE(903, "无可用车辆"),
    DISPATCH_ALGORITHM_ERROR(904, "调度算法执行失败"),

    /**
     * 任务相关错误
     */
    TASK_NOT_FOUND(910, "调度任务不存在"),
    TASK_STATUS_ERROR(911, "任务状态错误"),
    TASK_OPERATION_FAILED(912, "任务操作失败"),
    TASK_CANCELLED(913, "任务已取消"),

    /**
     * 路线相关错误
     */
    ROUTE_TEMPLATE_CREATE_FAILED(920, "路线模板创建失败"),
    ROUTE_TEMPLATE_NOT_FOUND(921, "路线模板不存在"),
    ROUTE_BLOCK_ADJUST_FAILED(922, "线路堵塞调整失败"),

    /**
     * 车辆相关错误
     */
    VEHICLE_NOT_FOUND(930, "车辆不存在"),
    VEHICLE_NOT_AVAILABLE(931, "车辆不可用"),
    VEHICLE_FAULT_ADJUST_FAILED(932, "车辆故障调整失败"),

    /**
     * 司机相关错误
     */
    DRIVER_NOT_FOUND(940, "司机不存在"),
    DRIVER_NOT_AVAILABLE(941, "司机不可用"),
    DRIVER_LEAVE_ADJUST_FAILED(942, "司机请假调整失败"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(999, "系统错误");

    private final int code;
    private final String message;
}
