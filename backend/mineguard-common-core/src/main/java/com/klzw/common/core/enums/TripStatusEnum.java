package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum TripStatusEnum {
    PENDING(1, "待开始"),
    IN_PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int value;
    private final String label;

    TripStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }
}
