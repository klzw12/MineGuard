package com.klzw.service.vehicle.enums;

/**
 * 保险状态枚举
 */
public enum InsuranceStatusEnum {
    /**
     * 未投保
     */
    NOT_INSURED(0, "未投保"),
    /**
     * 已投保
     */
    INSURED(1, "已投保"),
    /**
     * 已过期
     */
    EXPIRED(2, "已过期"),
    /**
     * 理赔中
     */
    CLAIMING(3, "理赔中");

    private final int code;
    private final String name;

    InsuranceStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static InsuranceStatusEnum getByCode(int code) {
        for (InsuranceStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}