package com.klzw.service.warning.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.warning.exception.WarningException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class WarningExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof WarningException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        WarningException warningException = (WarningException) throwable;
        return Result.fail(warningException.getCode(), warningException.getMessage());
    }
}
