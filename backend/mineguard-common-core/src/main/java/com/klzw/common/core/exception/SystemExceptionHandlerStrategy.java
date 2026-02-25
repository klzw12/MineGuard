package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;

public class SystemExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof SystemException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        SystemException ex = (SystemException) throwable;
        return Result.fail(ex.getCode(), ex.getMessage());
    }
}
