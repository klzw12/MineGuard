package com.klzw.service.vehicle.enums;

/**
 * 故障状态枚举
 */
public enum FaultStatusEnum {
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
     * 已关闭
     */
    CLOSED(3, "已关闭");

    private final int code;
    private final String name;

    FaultStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static FaultStatusEnum getByCode(int code) {
        for (FaultStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}