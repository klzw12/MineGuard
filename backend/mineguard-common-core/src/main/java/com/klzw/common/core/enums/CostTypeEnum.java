package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 成本类型枚举
 * 对应db.sql中cost_record表的cost_type字段
 */
@Getter
public enum CostTypeEnum {
    FUEL(1, "燃油费"),
    MAINTENANCE(2, "维修费"),
    INSURANCE(3, "保险费"),
    LABOR(4, "人工费"),
    OTHER(5, "其他");

    private final int value;
    private final String label;

    CostTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static CostTypeEnum getByValue(int value) {
        for (CostTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
