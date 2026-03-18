package com.klzw.service.vehicle.enums;

public enum VehicleSpecialStatusEnum {
    NORMAL(0, "正常"),
    LOW_FUEL(1, "油量不足"),
    BROKEN_DOWN(2, "车辆抛锚"),
    ACCIDENT(3, "发生车祸"),
    EMERGENCY(4, "紧急情况");

    private final int code;
    private final String desc;

    VehicleSpecialStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static VehicleSpecialStatusEnum getByCode(int code) {
        for (VehicleSpecialStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
