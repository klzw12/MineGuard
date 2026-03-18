package com.klzw.service.trip.enums;

/**
 * 行程状态枚举
 */
public enum TripStatusEnum {
    /**
     * 待开始
     */
    PENDING(0, "待开始"),
    /**
     * 已接单
     */
    ACCEPTED(1, "已接单"),
    /**
     * 进行中
     */
    IN_PROGRESS(2, "进行中"),
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),
    /**
     * 暂停中
     */
    PAUSED(5, "暂停中");

    private final int code;
    private final String name;

    TripStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static TripStatusEnum getByCode(int code) {
        for (TripStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}