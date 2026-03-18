package com.klzw.service.ai.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.ai.exception.AiException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class AiExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof AiException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        AiException exception = (AiException) throwable;
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
