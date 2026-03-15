package com.klzw.service.gateway.handler;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 全局异常处理器单元测试
 * 测试优先级：高 - 核心可靠性组件
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebFluxExceptionHandlerStrategy exceptionStrategy;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpResponse response;

    private GatewayExceptionHandler gatewayExceptionHandler;

    @BeforeEach
    void setUp() {
        gatewayExceptionHandler = new GatewayExceptionHandler(exceptionStrategy);
    }

    @Test
    void testHandle_ResponseCommitted_ReturnMonoError() {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(true);

        // When & Then
        StepVerifier.create(gatewayExceptionHandler.handle(exchange, exception))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testHandle_ResponseStatusException_ReturnErrorResponse() {
        // Given
        String message = "Not Found";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(gatewayExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
    }

    @Test
    void testHandle_NotFoundException_ReturnServiceUnavailable() {
        // Given
        NotFoundException exception = new NotFoundException("Service not found");
        
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(gatewayExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void testHandle_GeneralException_UseStrategy() {
        // Given
        RuntimeException exception = new RuntimeException("General error");
        
        when(exchange.getResponse()).thenReturn(response);
        when(response.isCommitted()).thenReturn(false);
        when(response.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(exceptionStrategy.handle(exception)).thenReturn(Result.fail(500, "Internal server error"));

        // When & Then
        StepVerifier.create(gatewayExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        verify(exceptionStrategy).handle(exception);
    }
}
