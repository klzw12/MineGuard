package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 预算状态枚举
 * 对应db.sql中budget表的status字段
 */
@Getter
public enum BudgetStatusEnum {
    VALID(1, "有效"),
    EXECUTED(2, "已执行");

    private final int value;
    private final String label;

    BudgetStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static BudgetStatusEnum getByValue(int value) {
        for (BudgetStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
