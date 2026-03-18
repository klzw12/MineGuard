package com.klzw.service.statistics.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.exception.StatisticsException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class StatisticsExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof StatisticsException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        StatisticsException exception = (StatisticsException) throwable;
        return Result.fail(exception.getCode(), exception.getMessage());
    }
}
