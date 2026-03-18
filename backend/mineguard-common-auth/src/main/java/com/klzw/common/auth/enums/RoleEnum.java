package com.klzw.common.auth.enums;

/**
 * 角色枚举
 */
public enum RoleEnum {

    /**
     * 管理员角色
     */
    ADMIN("ADMIN"),

    /**
     * 司机角色
     */
    DRIVER("DRIVER"),

    /**
     * 运营人员角色
     */
    OPERATOR("OPERATOR"),

    /**
     * 维修员角色
     */
    REPAIRMAN("REPAIRMAN"),

    /**
     * 安全员角色
     */
    SAFETY_OFFICER("SAFETY_OFFICER");

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
