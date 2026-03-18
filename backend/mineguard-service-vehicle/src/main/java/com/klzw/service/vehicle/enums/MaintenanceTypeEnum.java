package com.klzw.service.vehicle.enums;

/**
 * 维护类型枚举
 */
public enum MaintenanceTypeEnum {
    /**
     * 常规保养
     */
    ROUTINE(1, "常规保养"),
    /**
     * 小修
     */
    MINOR_REPAIR(2, "小修"),
    /**
     * 中修
     */
    MEDIUM_REPAIR(3, "中修"),
    /**
     * 大修
     */
    MAJOR_REPAIR(4, "大修"),
    /**
     * 年检
     */
    INSPECTION(5, "年检");

    private final int code;
    private final String name;

    MaintenanceTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MaintenanceTypeEnum getByCode(int code) {
        for (MaintenanceTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}