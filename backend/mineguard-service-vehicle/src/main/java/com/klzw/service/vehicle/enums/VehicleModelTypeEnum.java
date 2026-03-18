package com.klzw.service.vehicle.enums;

/**
 * 车辆型号类型枚举
 */
public enum VehicleModelTypeEnum {
    /**
     * 微型车
     */
    MINI(1, "微型车"),
    /**
     * 小型车
     */
    SMALL(2, "小型车"),
    /**
     * 中型车
     */
    MEDIUM(3, "中型车"),
    /**
     * 大型车
     */
    LARGE(4, "大型车"),
    /**
     * 重型车
     */
    HEAVY(5, "重型车");

    private final int code;
    private final String name;

    VehicleModelTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static VehicleModelTypeEnum getByCode(int code) {
        for (VehicleModelTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}