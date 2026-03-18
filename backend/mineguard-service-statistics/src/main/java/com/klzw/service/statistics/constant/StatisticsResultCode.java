package com.klzw.service.statistics.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatisticsResultCode {

    STATISTICS_DATA_NOT_FOUND(2200, "统计数据不存在"),
    REPORT_NOT_FOUND(2210, "报表不存在"),
    REPORT_GENERATE_FAILED(2211, "报表生成失败"),
    EXPORT_FAILED(2220, "导出失败");

    private final int code;
    private final String message;
}
