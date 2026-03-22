package com.klzw.service.cost.enums;

public enum BudgetStatusEnum {

    DRAFT(0, "草稿"),
    APPROVED(1, "已审批"),
    ACTIVE(2, "生效中"),
    IN_PROGRESS(3, "执行中"),
    COMPLETED(4, "已完成"),
    EXPIRED(5, "已过期");

    private final int code;
    private final String name;

    BudgetStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BudgetStatusEnum getByCode(int code) {
        for (BudgetStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
