package com.klzw.common.auth.enums;

/**
 * 角色枚举
 */
public enum RoleEnum {

    /**
     * 管理员角色
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * 普通管理员角色
     */
    MANAGER("ROLE_MANAGER"),

    /**
     * 司机角色
     */
    DRIVER("ROLE_DRIVER"),

    /**
     * 安全员角色
     */
    SAFETY("ROLE_SAFETY"),

    /**
     * 维修员角色
     */
    REPAIR("ROLE_REPAIR");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据值获取枚举
     */
    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }
}
