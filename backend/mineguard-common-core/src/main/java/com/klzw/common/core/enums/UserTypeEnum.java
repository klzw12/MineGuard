package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {
    ADMIN(1, "管理员"),
    DRIVER(2, "司机"),
    OPERATOR(3, "运营人员"),
    REPAIRMAN(4, "维修员"),
    SECURITY(5, "安全员");

    private final int value;
    private final String label;

    UserTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static UserTypeEnum getByValue(int value) {
        for (UserTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}