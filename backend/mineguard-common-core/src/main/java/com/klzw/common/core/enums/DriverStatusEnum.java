package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 司机状态枚举
 * 对应db.sql中driver表的status字段
 */
@Getter
public enum DriverStatusEnum {
    RESIGNED(0, "离职"),
    ON_DUTY(1, "在职");

    private final int value;
    private final String label;

    DriverStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static DriverStatusEnum getByValue(int value) {
        for (DriverStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
