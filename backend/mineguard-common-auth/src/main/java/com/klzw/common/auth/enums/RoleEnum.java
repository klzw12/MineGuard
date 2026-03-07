package com.klzw.common.auth.enums;

import lombok.Getter;

/**
 * 角色枚举
 * 与db.sql中role表的数据保持一致
 */
@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN", "管理员"),
    MANAGER("ROLE_MANAGER", "普通管理员"),
    DRIVER("ROLE_DRIVER", "司机"),
    SAFETY("ROLE_SAFETY", "安全员"),
    REPAIR("ROLE_REPAIR", "维修员");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum fromCode(String code) {
        for (RoleEnum roleEnum : values()) {
            if (roleEnum.getCode().equals(code)) {
                return roleEnum;
            }
        }
        return null;
    }
}
