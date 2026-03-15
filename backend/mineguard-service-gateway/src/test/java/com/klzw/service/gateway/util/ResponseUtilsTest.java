package com.klzw.service.gateway.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 响应工具类单元测试
 * 测试优先级：高 - 核心响应处理组件
 */
@ExtendWith(MockitoExtension.class)
class ResponseUtilsTest {

    @Mock
    private ServerHttpResponse response;

    @Test
    void testWriteError_Success() {
        // Given
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        int code = 401;
        String message = "Unauthorized";
        
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(ResponseUtils.writeError(response, status, code, message))
                .verifyComplete();

        verify(response).setStatusCode(status);
        verify(response).getHeaders();
        verify(response).bufferFactory();
        verify(response).writeWith(any());
    }

    @Test
    void testUnauthorized() {
        // Given
        String message = "Token missing";
        
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(ResponseUtils.unauthorized(response, message))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testForbidden() {
        // Given
        String message = "Access denied";
        
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(ResponseUtils.forbidden(response, message))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void testServerError() {
        // Given
        String message = "Internal server error";
        
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(ResponseUtils.serverError(response, message))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testServiceUnavailable() {
        // Given
        String message = "Service unavailable";
        
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());
        when(response.writeWith(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(ResponseUtils.serviceUnavailable(response, message))
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
