package com.klzw.service.dispatch.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调度模块错误码枚举
 * <p>
 * 错误码范围：1000-1099
 * <p>
 * 错误码说明：
 * - 1000: 调度通用错误
 * - 1001-1009: 调度相关错误
 * - 1010-1019: 任务相关错误
 * - 1020-1029: 路线相关错误
 * - 1030-1039: 车辆相关错误
 * - 1040-1049: 司机相关错误
 */
@Getter
@AllArgsConstructor
public enum DispatchResultCode {

    /**
     * 调度通用错误
     */
    DISPATCH_FAILED(1000, "调度失败"),
    PARAMETER_ERROR(1001, "参数错误"),

    /**
     * 调度相关错误
     */
    NO_AVAILABLE_DRIVER(1002, "无可用司机"),
    NO_AVAILABLE_VEHICLE(1003, "无可用车辆"),
    NO_AVAILABLE_REPAIRMAN(1004, "无可用维修员"),
    NO_AVAILABLE_SAFETY_OFFICER(1005, "无可用安全员"),
    DISPATCH_ALGORITHM_ERROR(1006, "调度算法执行失败"),

    /**
     * 任务相关错误
     */
    TASK_NOT_FOUND(1010, "调度任务不存在"),
    TASK_STATUS_ERROR(1011, "任务状态错误"),
    TASK_OPERATION_FAILED(1012, "任务操作失败"),
    TASK_CANCELLED(1013, "任务已取消"),

    /**
     * 路线相关错误
     */
    ROUTE_TEMPLATE_CREATE_FAILED(1020, "路线模板创建失败"),
    ROUTE_TEMPLATE_NOT_FOUND(1021, "路线模板不存在"),
    ROUTE_BLOCK_ADJUST_FAILED(1022, "线路堵塞调整失败"),

    /**
     * 车辆相关错误
     */
    VEHICLE_NOT_FOUND(1030, "车辆不存在"),
    VEHICLE_NOT_AVAILABLE(1031, "车辆不可用"),
    VEHICLE_FAULT_ADJUST_FAILED(1032, "车辆故障调整失败"),

    /**
     * 司机相关错误
     */
    DRIVER_NOT_FOUND(1040, "司机不存在"),
    DRIVER_NOT_AVAILABLE(1041, "司机不可用"),
    DRIVER_LEAVE_ADJUST_FAILED(1042, "司机请假调整失败"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(1099, "系统错误");

    private final int code;
    private final String message;
}
