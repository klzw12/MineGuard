package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import com.klzw.common.core.result.ResultCode;

public class DefaultExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return true;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        return Result.fail(ResultCode.INTERNAL_ERROR, "系统内部错误: " + throwable.getMessage());
    }
}
