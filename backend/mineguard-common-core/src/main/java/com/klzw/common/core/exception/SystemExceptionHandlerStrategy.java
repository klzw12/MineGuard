package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemExceptionHandlerStrategy implements ExceptionHandlerStrategy {
    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof SystemException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        SystemException ex = (SystemException) throwable;
        if (ex.getCause() != null) {
            log.error("系统异常: code={}, message={}, caused by: {}", ex.getCode(), ex.getMessage(), ex.getCause().getMessage(), ex.getCause());
        } else {
            log.error("系统异常: code={}, message={}", ex.getCode(), ex.getMessage());
        }
        return Result.fail(ex.getCode(), ex.getMessage());
    }
}
