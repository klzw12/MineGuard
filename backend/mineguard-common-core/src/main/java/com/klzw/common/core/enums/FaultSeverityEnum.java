package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 故障严重程度枚举
 * 对应db.sql中vehicle_fault表的severity字段
 */
@Getter
public enum FaultSeverityEnum {
    MINOR(1, "轻微"),
    GENERAL(2, "一般"),
    SEVERE(3, "严重");

    private final int value;
    private final String label;

    FaultSeverityEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static FaultSeverityEnum getByValue(int value) {
        for (FaultSeverityEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
