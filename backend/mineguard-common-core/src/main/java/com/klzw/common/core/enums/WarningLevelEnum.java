package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 预警级别枚举
 * 对应db.sql中warning_rule和warning_record表的warning_level字段
 */
@Getter
public enum WarningLevelEnum {
    INFO(1, "提示"),
    WARNING(2, "警告"),
    CRITICAL(3, "严重");

    private final int value;
    private final String label;

    WarningLevelEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static WarningLevelEnum getByValue(int value) {
        for (WarningLevelEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
