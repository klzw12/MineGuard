package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 保养类型枚举
 * 对应db.sql中vehicle_maintenance表的maintenance_type字段
 */
@Getter
public enum MaintenanceTypeEnum {
    REGULAR(1, "常规保养"),
    OVERHAUL(2, "大修"),
    FAULT_REPAIR(3, "故障维修");

    private final int value;
    private final String label;

    MaintenanceTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static MaintenanceTypeEnum getByValue(int value) {
        for (MaintenanceTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
