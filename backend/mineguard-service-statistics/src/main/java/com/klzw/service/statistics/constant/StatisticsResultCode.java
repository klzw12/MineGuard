package com.klzw.service.statistics.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计模块错误码枚举
 * <p>
 * 错误码范围：2400-2499
 */
@Getter
@AllArgsConstructor
public enum StatisticsResultCode {

    STATISTICS_DATA_NOT_FOUND(2400, "统计数据不存在"),
    REPORT_NOT_FOUND(2410, "报表不存在"),
    REPORT_GENERATE_FAILED(2411, "报表生成失败"),
    EXPORT_FAILED(2420, "导出失败");

    private final int code;
    private final String message;
}
