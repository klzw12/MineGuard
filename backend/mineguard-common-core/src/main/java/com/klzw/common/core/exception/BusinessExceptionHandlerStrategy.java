package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;

public class BusinessExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof BusinessException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        BusinessException ex = (BusinessException) throwable;
        return Result.fail(ex.getCode(), ex.getMessage());
    }
}
