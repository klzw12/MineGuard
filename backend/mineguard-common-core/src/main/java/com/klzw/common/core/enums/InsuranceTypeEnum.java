package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 保险类型枚举
 * 对应db.sql中vehicle_insurance表的insurance_type字段
 */
@Getter
public enum InsuranceTypeEnum {
    COMPULSORY(1, "交强险"),
    COMMERCIAL(2, "商业险");

    private final int value;
    private final String label;

    InsuranceTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static InsuranceTypeEnum getByValue(int value) {
        for (InsuranceTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
