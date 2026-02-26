package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final int value;
    private final String label;

    UserStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static UserStatusEnum getByValue(int value) {
        for (UserStatusEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
