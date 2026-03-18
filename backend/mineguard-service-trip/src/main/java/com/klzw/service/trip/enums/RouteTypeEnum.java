package com.klzw.service.trip.enums;

/**
 * 路线类型枚举
 */
public enum RouteTypeEnum {
    /**
     * 最短距离
     */
    SHORTEST(1, "最短距离"),
    /**
     * 最快时间
     */
    FASTEST(2, "最快时间"),
    /**
     * 最优路线
     */
    OPTIMAL(3, "最优路线"),
    /**
     * 自定义
     */
    CUSTOM(4, "自定义");

    private final int code;
    private final String name;

    RouteTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RouteTypeEnum getByCode(int code) {
        for (RouteTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}