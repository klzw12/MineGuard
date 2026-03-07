package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 操作日志状态枚举
 * 对应db.sql中operation_log表的status字段
 */
@Getter
public enum OperationLogStatusEnum {
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final int value;
    private final String label;

    OperationLogStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static OperationLogStatusEnum getByValue(int value) {
        for (OperationLogStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
