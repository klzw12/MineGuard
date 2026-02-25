package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum VehicleStatusEnum {
    IDLE(1, "空闲"),
    BUSY(2, "忙碌"),
    MAINTAIN(3, "维护"),
    FAULT(4, "故障");

    private final int value;
    private final String label;

    VehicleStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }
}
