package com.klzw.common.core.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    BUSINESS_ERROR(600, "业务错误"),
    PARAM_ERROR(601, "参数错误"),
    DATA_ERROR(602, "数据错误"),
    PERMISSION_ERROR(603, "权限错误");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
