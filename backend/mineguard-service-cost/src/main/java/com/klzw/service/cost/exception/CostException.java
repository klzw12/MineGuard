package com.klzw.service.cost.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.cost.constant.CostResultCode;
import lombok.Getter;

@Getter
public class CostException extends BaseException {

    private static final String MODULE = "cost";

    public CostException(int code, String message) {
        super(code, message, MODULE);
    }

    public CostException(CostResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public CostException(CostResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }
}
