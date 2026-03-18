package com.klzw.service.warning.enums;

/**
 * 预警级别枚举
 */
public enum WarningLevelEnum {
    /**
     * 低风险
     */
    LOW(1, "低风险"),
    /**
     * 中风险
     */
    MEDIUM(2, "中风险"),
    /**
     * 高风险
     */
    HIGH(3, "高风险");

    private final int code;
    private final String name;

    WarningLevelEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static WarningLevelEnum getByCode(int code) {
        for (WarningLevelEnum level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return null;
    }
}