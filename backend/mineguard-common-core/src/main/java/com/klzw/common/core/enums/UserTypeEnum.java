package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {
    ADMIN(1, "管理员"),
    DRIVER(2, "司机"),
    MAINTENANCE(3, "维修员"),
    SAFETY(4, "安全员");

    private final int value;
    private final String label;

    UserTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }
}
