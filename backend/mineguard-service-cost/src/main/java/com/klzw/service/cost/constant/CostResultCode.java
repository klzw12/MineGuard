package com.klzw.service.cost.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成本模块错误码枚举
 * <p>
 * 错误码范围：2500-2599
 */
@Getter
@AllArgsConstructor
public enum CostResultCode {

    COST_RECORD_NOT_FOUND(2500, "成本记录不存在"),
    BUDGET_NOT_FOUND(2510, "预算不存在"),
    BUDGET_EXCEEDED(2511, "预算已超支"),
    BUDGET_DISABLED(2512, "预算已禁用"),
    BILLING_RULE_NOT_FOUND(2520, "计费规则不存在");

    private final int code;
    private final String message;
}
