package com.klzw.service.vehicle.exception;

import com.klzw.common.core.exception.BaseException;
import lombok.Getter;

/**
 * 车辆模块业务异常
 * <p>
 * 用于处理车辆模块相关的业务异常，包括：
 * - 车辆不存在
 * - 车辆状态异常
 * - 故障记录异常
 * - 保险记录异常
 * - 保养记录异常
 * - 加油记录异常
 * <p>
 * 错误码范围：1000-1099（统一使用 VehicleResultCode 定义）
 *
 * @see VehicleResultCode
 */
@Getter
public class VehicleException extends BaseException {

    /**
     * 车辆模块标识
     */
    private static final String MODULE = "vehicle";

    /**
     * 构造方法 - 使用 VehicleResultCode
     *
     * @param resultCode 车辆错误码枚举
     */
    public VehicleException(VehicleResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    /**
     * 构造方法 - 使用 VehicleResultCode 和自定义消息
     *
     * @param resultCode 车辆错误码枚举
     * @param message    自定义错误消息
     */
    public VehicleException(VehicleResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    /**
     * 构造方法 - 使用 VehicleResultCode 和异常原因
     *
     * @param resultCode 车辆错误码枚举
     * @param cause      异常原因
     */
    public VehicleException(VehicleResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    /**
     * 构造方法 - 使用 VehicleResultCode、自定义消息和异常原因
     *
     * @param resultCode 车辆错误码枚举
     * @param message    自定义错误消息
     * @param cause      异常原因
     */
    public VehicleException(VehicleResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, MODULE, cause);
    }

    /**
     * 构造方法 - 使用错误码和消息（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public VehicleException(int code, String message) {
        super(code, message, MODULE);
    }

    /**
     * 构造方法 - 使用错误码、消息和异常原因（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   异常原因
     */
    public VehicleException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }
}
