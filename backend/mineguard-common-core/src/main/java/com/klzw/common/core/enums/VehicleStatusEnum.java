package com.klzw.common.core.enums;

/**
 * 车辆状态枚举（跨服务共享，定义在core模块）
 */
public enum VehicleStatusEnum {
    IDLE(0, "空闲"),
    RUNNING(1, "运行中"),
    MAINTENANCE(2, "维护中"),
    FAULT(3, "故障"),
    SCRAPPED(4, "报废");

    private final int code;
    private final String name;

    VehicleStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static VehicleStatusEnum getByCode(int code) {
        for (VehicleStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
