package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 预警类型枚举
 * 对应db.sql中warning_rule和warning_record表的warning_type字段
 */
@Getter
public enum WarningTypeEnum {
    SPEED(1, "超速"),
    ROUTE_DEVIATION(2, "偏离路线"),
    THEFT(3, "盗卸"),
    FATIGUE(4, "疲劳驾驶"),
    STOP_TIMEOUT(5, "停留超时");

    private final int value;
    private final String label;

    WarningTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static WarningTypeEnum getByValue(int value) {
        for (WarningTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
