package com.klzw.service.gateway.filter;

import com.klzw.service.gateway.constant.GatewayConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 链路追踪过滤器单元测试
 * 测试优先级：高 - 核心可观测性组件
 */
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private Builder requestBuilder;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders headers;

    @Mock
    private ServerWebExchange.Builder exchangeBuilder;

    private TraceIdFilter traceIdFilter;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdFilter();
    }

    @Test
    void testFilter_WithExistingTraceId_KeepTraceId() {
        // Given
        String existingTraceId = "existing-trace-id-123";
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(existingTraceId);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(eq(GatewayConstant.HEADER_TRACE_ID), eq(existingTraceId))).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request((org.springframework.http.server.reactive.ServerHttpRequest) any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(traceIdFilter.filter(exchange, chain))
                .verifyComplete();

        verify(requestBuilder).header(GatewayConstant.HEADER_TRACE_ID, existingTraceId);
        verify(chain).filter(any());
    }

    @Test
    void testFilter_NoTraceId_GenerateNewTraceId() {
        // Given
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(null);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(eq(GatewayConstant.HEADER_TRACE_ID), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request((org.springframework.http.server.reactive.ServerHttpRequest) any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(traceIdFilter.filter(exchange, chain))
                .verifyComplete();

        verify(requestBuilder).header(eq(GatewayConstant.HEADER_TRACE_ID), anyString());
        verify(chain).filter(any());
    }

    @Test
    void testFilter_EmptyTraceId_GenerateNewTraceId() {
        // Given
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn("");
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(eq(GatewayConstant.HEADER_TRACE_ID), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request((org.springframework.http.server.reactive.ServerHttpRequest) any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(traceIdFilter.filter(exchange, chain))
                .verifyComplete();

        verify(requestBuilder).header(eq(GatewayConstant.HEADER_TRACE_ID), anyString());
        verify(chain).filter(any());
    }

    @Test
    void testGetOrder_ReturnCorrectOrder() {
        assertEquals(GatewayConstant.FILTER_ORDER_TRACE, traceIdFilter.getOrder());
    }
}
