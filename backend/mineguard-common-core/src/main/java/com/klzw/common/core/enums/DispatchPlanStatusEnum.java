package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 调度计划状态枚举
 * 对应db.sql中dispatch_plan表的status字段
 */
@Getter
public enum DispatchPlanStatusEnum {
    PENDING(1, "待执行"),
    IN_PROGRESS(2, "执行中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int value;
    private final String label;

    DispatchPlanStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static DispatchPlanStatusEnum getByValue(int value) {
        for (DispatchPlanStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
