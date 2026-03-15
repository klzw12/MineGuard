package com.klzw.service.gateway.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 熔断降级处理器单元测试
 * 测试优先级：高 - 核心可靠性组件
 */
@ExtendWith(MockitoExtension.class)
class FallbackHandlerTest {

    @Mock
    private ServerRequest request;

    private FallbackHandler fallbackHandler;

    @BeforeEach
    void setUp() {
        fallbackHandler = new FallbackHandler();
    }

    @Test
    void testFallback_WithException() {
        // Given
        String serviceName = "/api/user-service";
        RuntimeException exception = new RuntimeException("Service unavailable");

        when(request.path()).thenReturn(serviceName);
        when(request.attribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR))
                .thenReturn(java.util.Optional.of(exception));

        // When & Then
        StepVerifier.create(fallbackHandler.fallback(request))
                .expectNextMatches(response -> {
                    assert response != null;
                    return response.statusCode().value() == 503;
                })
                .verifyComplete();
    }

    @Test
    void testFallback_NoException() {
        // Given
        String serviceName = "/api/user-service";

        when(request.path()).thenReturn(serviceName);
        when(request.attribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        StepVerifier.create(fallbackHandler.fallback(request))
                .expectNextMatches(response -> {
                    assert response != null;
                    return response.statusCode().value() == 503;
                })
                .verifyComplete();
    }
}
