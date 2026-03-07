package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 路线状态枚举
 * 对应db.sql中route表的status字段
 */
@Getter
public enum RouteStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(2, "禁用");

    private final int value;
    private final String label;

    RouteStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static RouteStatusEnum getByValue(int value) {
        for (RouteStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
