package com.klzw.common.auth.enums;

import lombok.Getter;

/**
 * 权限类型枚举
 * 对应db.sql中permission表的permission_type字段
 */
@Getter
public enum PermissionTypeEnum {
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    private final int value;
    private final String label;

    PermissionTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static PermissionTypeEnum getByValue(int value) {
        for (PermissionTypeEnum item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
}
