package com.klzw.common.core.enums;

import lombok.Getter;

/**
 * 车型类型枚举
 * 对应db.sql中vehicle_model表的vehicle_type字段
 */
@Getter
public enum VehicleModelTypeEnum {
    NORMAL(1, "普通车辆"),
    RESCUE(2, "救援车"),
    MAINTENANCE(3, "维修车");

    private final int value;
    private final String label;

    VehicleModelTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static VehicleModelTypeEnum getByValue(int value) {
        for (VehicleModelTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
