package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();

    @ExceptionHandler(Throwable.class)
    public Result<?> handleException(Throwable throwable) {
        log.error("Exception occurred:", throwable);
        
        ExceptionHandlerStrategy strategy = registry.getStrategy(throwable);
        return strategy.handle(throwable);
    }

    public void registerStrategy(ExceptionHandlerStrategy strategy) {
        registry.register(strategy);
    }
}
