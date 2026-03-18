package com.klzw.service.trip.enums;

/**
 * 路线状态枚举
 */
public enum RouteStatusEnum {
    /**
     * 规划中
     */
    PLANNING(0, "规划中"),
    /**
     * 已规划
     */
    PLANNED(1, "已规划"),
    /**
     * 执行中
     */
    EXECUTING(2, "执行中"),
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final int code;
    private final String name;

    RouteStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RouteStatusEnum getByCode(int code) {
        for (RouteStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}