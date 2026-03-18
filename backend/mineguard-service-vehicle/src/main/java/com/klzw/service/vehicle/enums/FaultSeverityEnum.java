package com.klzw.service.vehicle.enums;

/**
 * 故障严重程度枚举
 */
public enum FaultSeverityEnum {
    /**
     * 轻微
     */
    MINOR(1, "轻微"),
    /**
     * 中等
     */
    MODERATE(2, "中等"),
    /**
     * 严重
     */
    SEVERE(3, "严重"),
    /**
     * 紧急
     */
    EMERGENCY(4, "紧急");

    private final int code;
    private final String name;

    FaultSeverityEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static FaultSeverityEnum getByCode(int code) {
        for (FaultSeverityEnum severity : values()) {
            if (severity.code == code) {
                return severity;
            }
        }
        return null;
    }
}