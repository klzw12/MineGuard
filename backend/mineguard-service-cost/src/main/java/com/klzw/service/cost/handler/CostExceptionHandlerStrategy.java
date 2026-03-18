package com.klzw.service.cost.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.cost.exception.CostException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class CostExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof CostException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        CostException exception = (CostException) throwable;
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
