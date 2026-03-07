package com.klzw.service.gateway.handler;

import com.klzw.common.core.result.Result;
import com.klzw.service.gateway.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final WebFluxExceptionHandlerStrategy exceptionStrategy;

    public GlobalExceptionHandler(WebFluxExceptionHandlerStrategy exceptionStrategy) {
        this.exceptionStrategy = exceptionStrategy;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            HttpStatus status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            String message = responseStatusException.getReason();
            log.warn("ResponseStatusException: {} - {}", status, message);
            return ResponseUtils.writeError(response, status, status.value(), message);
        }

        if (ex instanceof NotFoundException) {
            log.warn("Service not found: {}", ex.getMessage());
            return ResponseUtils.writeError(response, HttpStatus.SERVICE_UNAVAILABLE, 503, "服务不可用");
        }

        log.error("Gateway error: {}", ex.getMessage(), ex);
        Result<?> result = exceptionStrategy.handle(ex);
        return ResponseUtils.writeError(response, 
                HttpStatus.valueOf(result.getCode()), 
                result.getCode(), 
                result.getMessage());
    }
}
