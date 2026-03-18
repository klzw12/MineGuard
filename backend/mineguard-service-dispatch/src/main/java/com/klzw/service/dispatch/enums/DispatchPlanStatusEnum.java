package com.klzw.service.dispatch.enums;

/**
 * 调度计划状态枚举
 */
public enum DispatchPlanStatusEnum {
    /**
     * 待执行
     */
    PENDING(0, "待执行"),
    /**
     * 执行中
     */
    IN_EXECUTION(1, "执行中"),
    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),
    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    private final int code;
    private final String name;

    DispatchPlanStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DispatchPlanStatusEnum getByCode(int code) {
        for (DispatchPlanStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}