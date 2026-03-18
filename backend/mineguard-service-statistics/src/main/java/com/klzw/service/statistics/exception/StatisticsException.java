package com.klzw.service.statistics.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.statistics.constant.StatisticsResultCode;
import lombok.Getter;

@Getter
public class StatisticsException extends BaseException {

    private static final String MODULE = "statistics";

    public StatisticsException(int code, String message) {
        super(code, message, MODULE);
    }

    public StatisticsException(StatisticsResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public StatisticsException(StatisticsResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }
}
