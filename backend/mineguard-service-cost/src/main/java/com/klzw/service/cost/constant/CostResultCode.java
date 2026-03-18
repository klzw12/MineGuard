package com.klzw.service.cost.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CostResultCode {

    COST_RECORD_NOT_FOUND(2300, "成本记录不存在"),
    BUDGET_NOT_FOUND(2310, "预算不存在"),
    BUDGET_EXCEEDED(2311, "预算已超支"),
    BUDGET_DISABLED(2312, "预算已禁用"),
    BILLING_RULE_NOT_FOUND(2320, "计费规则不存在");

    private final int code;
    private final String message;
}
