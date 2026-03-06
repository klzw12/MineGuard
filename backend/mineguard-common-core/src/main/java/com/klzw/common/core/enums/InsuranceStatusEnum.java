package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 保险状态枚举
 * 对应db.sql中vehicle_insurance表的status字段
 */
@Getter
public enum InsuranceStatusEnum {
    EXPIRED(2, "过期"),
    VALID(1, "有效");

    private final int value;
    private final String label;

    InsuranceStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static InsuranceStatusEnum getByValue(int value) {
        for (InsuranceStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
