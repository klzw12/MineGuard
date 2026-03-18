package com.klzw.service.cost.enums;

/**
 * 预算状态枚举
 */
public enum BudgetStatusEnum {
    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),
    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),
    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),
    /**
     * 已超支
     */
    OVER_BUDGET(3, "已超支");

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