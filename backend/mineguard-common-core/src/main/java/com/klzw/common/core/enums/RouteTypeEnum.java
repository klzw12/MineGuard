package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 路线类型枚举
 * 对应db.sql中route表的route_type字段
 */
@Getter
public enum RouteTypeEnum {
    NORMAL(1, "常规路线"),
    BACKUP(2, "备用路线");

    private final int value;
    private final String label;

    RouteTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static RouteTypeEnum getByValue(int value) {
        for (RouteTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
