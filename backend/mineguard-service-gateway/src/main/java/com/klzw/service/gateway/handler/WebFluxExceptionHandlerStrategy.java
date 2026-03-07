package com.klzw.service.gateway.handler;

import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebFluxExceptionHandlerStrategy {

    private final ExceptionHandlerRegistry registry;

    public WebFluxExceptionHandlerStrategy(ExceptionHandlerRegistry registry) {
        this.registry = registry;
    }

    public Result<?> handle(Throwable ex) {
        ExceptionHandlerStrategy strategy = registry.getStrategy(ex);
        return strategy.handle(ex);
    }
}
