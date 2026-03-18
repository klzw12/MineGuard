package com.klzw.service.vehicle.enums;

/**
 * 保险类型枚举
 */
public enum InsuranceTypeEnum {
    /**
     * 交强险
     */
    COMPULSORY(1, "交强险"),
    /**
     * 商业险
     */
    COMMERCIAL(2, "商业险"),
    /**
     * 第三者责任险
     */
    THIRD_PARTY(3, "第三者责任险"),
    /**
     * 车辆损失险
     */
    VEHICLE_DAMAGE(4, "车辆损失险"),
    /**
     * 盗抢险
     */
    THEFT(5, "盗抢险");

    private final int code;
    private final String name;

    InsuranceTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static InsuranceTypeEnum getByCode(int code) {
        for (InsuranceTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}