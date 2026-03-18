package com.klzw.service.warning.enums;

/**
 * 预警类型枚举
 */
public enum WarningTypeEnum {
    /**
     * 路线偏离
     */
    ROUTE_DEVIATION(1, "路线偏离"),
    /**
     * 长时间停留
     */
    LONG_STAY(2, "长时间停留"),
    /**
     * 车辆故障
     */
    VEHICLE_FAULT(3, "车辆故障"),
    /**
     * 异常行为
     */
    ABNORMAL_BEHAVIOR(4, "异常行为"),
    /**
     * 危险区域
     */
    DANGER_ZONE(5, "危险区域");

    private final int code;
    private final String name;

    WarningTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static WarningTypeEnum getByCode(int code) {
        for (WarningTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}