package com.klzw.service.gateway.handler;

import com.klzw.common.core.result.Result;
import com.klzw.service.gateway.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 熔断降级处理器
 */
@Slf4j
@Component
public class FallbackHandler {

    /**
     * 服务降级处理
     */
    public Mono<ServerResponse> fallback(ServerRequest request) {
        Throwable exception = request.attribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR)
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .orElse(null);

        String serviceName = request.path();
        log.warn("Service fallback: {}, exception: {}", serviceName, 
                exception != null ? exception.getMessage() : "unknown");

        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyValue(Result.fail(503, "服务暂时不可用，请稍后重试"));
    }
}
