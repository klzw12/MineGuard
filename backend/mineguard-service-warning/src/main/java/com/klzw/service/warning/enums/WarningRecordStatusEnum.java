package com.klzw.service.warning.enums;

/**
 * 预警记录状态枚举
 */
public enum WarningRecordStatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),
    /**
     * 已解决
     */
    RESOLVED(2, "已解决"),
    /**
     * 已忽略
     */
    IGNORED(3, "已忽略");

    private final int code;
    private final String name;

    WarningRecordStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static WarningRecordStatusEnum getByCode(int code) {
        for (WarningRecordStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}