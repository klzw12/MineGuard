package com.klzw.common.auth.enums;

import lombok.Getter;

/**
 * 权限枚举
 */
@Getter
public enum PermissionEnum {

    USER_READ("user:read", "用户查询"),
    USER_WRITE("user:write", "用户管理"),
    VEHICLE_READ("vehicle:read", "车辆查询"),
    VEHICLE_WRITE("vehicle:write", "车辆管理"),
    TRIP_READ("trip:read", "行程查询"),
    TRIP_WRITE("trip:write", "行程管理"),
    WARNING_READ("warning:read", "预警查询"),
    WARNING_WRITE("warning:write", "预警管理"),
    SYSTEM_READ("system:read", "系统查询"),
    SYSTEM_WRITE("system:write", "系统管理");

    private final String code;
    private final String desc;

    PermissionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PermissionEnum fromCode(String code) {
        for (PermissionEnum permissionEnum : values()) {
            if (permissionEnum.getCode().equals(code)) {
                return permissionEnum;
            }
        }
        return null;
    }
}
