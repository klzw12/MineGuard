package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 预警记录处理状态枚举
 * 对应db.sql中warning_record表的status字段
 */
@Getter
public enum WarningRecordStatusEnum {
    PENDING(1, "未处理"),
    PROCESSING(2, "处理中"),
    RESOLVED(3, "已处理");

    private final int value;
    private final String label;

    WarningRecordStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static WarningRecordStatusEnum getByValue(int value) {
        for (WarningRecordStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
