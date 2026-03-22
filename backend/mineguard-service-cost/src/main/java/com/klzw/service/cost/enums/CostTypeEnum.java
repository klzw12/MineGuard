package com.klzw.service.cost.enums;

public enum CostTypeEnum {

    FUEL(1, "燃油成本"),
    MAINTENANCE(2, "维修成本"),
    LABOR(3, "人工成本"),
    INSURANCE(4, "保险成本"),
    DEPRECIATION(5, "折旧成本"),
    MANAGEMENT(6, "管理成本"),
    OTHER(7, "其他成本");

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
