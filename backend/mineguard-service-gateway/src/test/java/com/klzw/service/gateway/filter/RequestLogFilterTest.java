package com.klzw.service.gateway.filter;

import com.klzw.service.gateway.constant.GatewayConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * 请求日志过滤器单元测试
 * 测试优先级：高 - 核心可观测性组件
 */
@ExtendWith(MockitoExtension.class)
class RequestLogFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders headers;

    private RequestLogFilter requestLogFilter;

    @BeforeEach
    void setUp() {
        requestLogFilter = new RequestLogFilter();
    }

    @Test
    void testFilter_WithTraceId_LogRequestAndResponse() {
        // Given
        String traceId = "test-trace-id-123";
        String method = "GET";
        String path = "/api/test";
        String clientIp = "192.168.1.1";
        int statusCode = 200;

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(traceId);
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(clientIp, 8080));
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_NoTraceId_LogRequestAndResponse() {
        // Given
        String method = "POST";
        String path = "/api/test";
        String clientIp = "192.168.1.1";
        int statusCode = 201;

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(null);
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.POST);
        when(request.getPath()).thenReturn(requestPath);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(clientIp, 8080));
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.CREATED);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_NoRemoteAddress_UseUnknown() {
        // Given
        String traceId = "test-trace-id-123";
        String method = "GET";
        String path = "/api/test";

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(traceId);
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(request.getRemoteAddress()).thenReturn(null);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_XForwardedForHeader_Present() {
        // Given
        String traceId = "test-trace-id-123";
        String method = "GET";
        String path = "/api/test";
        String clientIp = "10.0.0.1";

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(traceId);
        when(headers.getFirst("X-Forwarded-For")).thenReturn(clientIp);
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_XRealIPHeader_Present() {
        // Given
        String traceId = "test-trace-id-123";
        String method = "GET";
        String path = "/api/test";
        String clientIp = "10.0.0.1";

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(traceId);
        when(headers.getFirst("X-Forwarded-For")).thenReturn(null);
        when(headers.getFirst("X-Real-IP")).thenReturn(clientIp);
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_MultipleXForwardedForValues() {
        // Given
        String traceId = "test-trace-id-123";
        String method = "GET";
        String path = "/api/test";

        org.springframework.http.server.RequestPath requestPath = mock(org.springframework.http.server.RequestPath.class);
        when(requestPath.value()).thenReturn(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(GatewayConstant.HEADER_TRACE_ID)).thenReturn(traceId);
        when(headers.getFirst("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1, 172.16.0.1");
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(requestLogFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testGetOrder_ReturnCorrectOrder() {
        assertEquals(GatewayConstant.FILTER_ORDER_LOG, requestLogFilter.getOrder());
    }
}
