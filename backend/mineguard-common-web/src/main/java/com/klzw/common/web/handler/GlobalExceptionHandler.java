package com.klzw.common.web.handler;

import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ExceptionHandlerRegistry registry;

    public GlobalExceptionHandler(ExceptionHandlerRegistry registry) {
        this.registry = registry;
    }

    @ExceptionHandler(Throwable.class)
    public Result<?> handleException(Throwable throwable) {
        log.error("Exception occurred:", throwable);
        
        ExceptionHandlerStrategy strategy = registry.getStrategy(throwable);
        return strategy.handle(throwable);
    }
}