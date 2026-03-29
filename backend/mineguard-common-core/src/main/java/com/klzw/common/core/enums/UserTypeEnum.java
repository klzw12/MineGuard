package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {
    ADMIN(1, "管理员", "ADMIN"),
    DRIVER(2, "司机", "DRIVER"),
    OPERATOR(3, "运营人员", "OPERATOR"),
    REPAIRMAN(4, "维修员", "REPAIRMAN"),
    SAFETY_OFFICER(5, "安全员", "SAFETY_OFFICER");

    private final int value;
    private final String label;
    private final String roleCode;

    UserTypeEnum(int value, String label, String roleCode) {
        this.value = value;
        this.label = label;
        this.roleCode = roleCode;
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