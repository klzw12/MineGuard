package com.klzw.service.warning.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警模块错误码枚举
 * <p>
 * 错误码范围：2200-2299
 */
@Getter
@AllArgsConstructor
public enum WarningResultCode {

    WARNING_RULE_NOT_FOUND(2200, "预警规则不存在"),
    WARNING_RULE_CODE_EXISTS(2201, "规则编码已存在"),
    WARNING_RULE_DISABLED(2202, "预警规则已禁用"),
    
    WARNING_RECORD_NOT_FOUND(2210, "预警记录不存在"),
    WARNING_ALREADY_HANDLED(2211, "预警已处理"),
    WARNING_ALREADY_CLOSED(2212, "预警已关闭"),
    
    WARNING_THRESHOLD_INVALID(2220, "预警阈值无效"),
    WARNING_NOTIFY_FAILED(2221, "预警通知发送失败");

    private final int code;
    private final String message;
}
