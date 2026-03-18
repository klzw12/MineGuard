package com.klzw.service.warning.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarningResultCode {

    WARNING_RULE_NOT_FOUND(2100, "预警规则不存在"),
    WARNING_RULE_CODE_EXISTS(2101, "规则编码已存在"),
    WARNING_RULE_DISABLED(2102, "预警规则已禁用"),
    
    WARNING_RECORD_NOT_FOUND(2110, "预警记录不存在"),
    WARNING_ALREADY_HANDLED(2111, "预警已处理"),
    WARNING_ALREADY_CLOSED(2112, "预警已关闭"),
    
    WARNING_THRESHOLD_INVALID(2120, "预警阈值无效"),
    WARNING_NOTIFY_FAILED(2121, "预警通知发送失败");

    private final int code;
    private final String message;
}
