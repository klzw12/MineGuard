package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 安全员状态枚举
 * 对应db.sql中safety_officer表的status字段
 */
@Getter
public enum SafetyOfficerStatusEnum {
    RESIGNED(0, "离职"),
    ON_DUTY(1, "在职");

    private final int value;
    private final String label;

    SafetyOfficerStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static SafetyOfficerStatusEnum getByValue(int value) {
        for (SafetyOfficerStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
