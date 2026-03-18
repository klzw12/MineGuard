package com.klzw.service.trip.handler;

import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.exception.TripException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class TripExceptionHandlerStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof TripException;
    }

    @Override
    public Result<?> handle(Throwable throwable) {
        TripException tripException = (TripException) throwable;
        return Result.fail(tripException.getCode(), tripException.getMessage());
    }
}
