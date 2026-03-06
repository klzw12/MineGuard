package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 维修员状态枚举
 * 对应db.sql中repairman表的status字段
 */
@Getter
public enum RepairmanStatusEnum {
    RESIGNED(0, "离职"),
    ON_DUTY(1, "在职");

    private final int value;
    private final String label;

    RepairmanStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static RepairmanStatusEnum getByValue(int value) {
        for (RepairmanStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
