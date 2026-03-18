package com.klzw.service.vehicle.enums;

/**
 * 车辆状态枚举
 */
public enum VehicleStatusEnum {
    /**
     * 空闲
     */
    IDLE(0, "空闲"),
    /**
     * 运行中
     */
    RUNNING(1, "运行中"),
    /**
     * 维护中
     */
    MAINTENANCE(2, "维护中"),
    /**
     * 故障
     */
    FAULT(3, "故障"),
    /**
     * 报废
     */
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