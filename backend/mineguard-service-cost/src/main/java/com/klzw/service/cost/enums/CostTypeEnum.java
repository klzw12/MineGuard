package com.klzw.service.cost.enums;

/**
 * 成本类型枚举
 */
public enum CostTypeEnum {
    /**
     * 燃油费
     */
    FUEL(1, "燃油费"),
    /**
     * 过路费
     */
    TOLL(2, "过路费"),
    /**
     * 维修费
     */
    MAINTENANCE(3, "维修费"),
    /**
     * 保险费
     */
    INSURANCE(4, "保险费"),
    /**
     * 罚款
     */
    FINE(5, "罚款"),
    /**
     * 其他
     */
    OTHER(6, "其他");

    private final int code;
    private final String name;

    CostTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static CostTypeEnum getByCode(int code) {
        for (CostTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}