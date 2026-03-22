package com.klzw.service.warning.enums;

public enum WarningTypeEnum {
    VEHICLE_FAULT(1, "车辆故障"),
    ROUTE_DEVIATION(2, "路线偏离"),
    LONG_STAY(3, "长时间停留"),
    DANGER_ZONE(4, "危险区域"),
    SPEED_ABNORMAL(5, "速度异常"),
    ABNORMAL_BEHAVIOR(6, "异常行为"),
    FATIGUE_DRIVING(7, "疲劳驾驶");

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
