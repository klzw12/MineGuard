package com.klzw.service.cost.enums;

public enum BudgetTypeEnum {

    MONTHLY(1, "月度"),
    QUARTERLY(2, "季度"),
    YEARLY(3, "年度");

    private final int code;
    private final String name;

    BudgetTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BudgetTypeEnum getByCode(int code) {
        for (BudgetTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
