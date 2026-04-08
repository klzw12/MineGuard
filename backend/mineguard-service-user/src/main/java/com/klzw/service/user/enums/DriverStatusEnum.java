package com.klzw.service.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DriverStatusEnum {

    RESIGNED(0, "离职"),
    EMPLOYED(1, "在职"),
    ON_LEAVE(2, "请假");

    private final Integer value;
    private final String label;

    public static DriverStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (DriverStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
