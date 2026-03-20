package com.klzw.service.gateway.filter;

import com.klzw.service.gateway.constant.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String traceId = request.getHeaders().getFirst(GatewayConstant.HEADER_TRACE_ID);
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);
        String uri = request.getURI() != null ? request.getURI().toString() : "unknown-uri";
        
        long startTime = System.currentTimeMillis();
        
        log.info("[{}] RequestLogFilter START - {} {} from {} URI: {}", traceId, method, path, clientIp, uri);
        
        // 添加debug日志，打印请求头信息
        log.debug("[{}] Request headers: {}", traceId, request.getHeaders());
        
        return chain.filter(exchange).doOnSuccess(void_ -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() 
                    : 0;
            String contentType = exchange.getResponse().getHeaders() != null 
                    ? exchange.getResponse().getHeaders().getFirst("Content-Type") 
                    : "unknown";
            
            // 添加debug日志，打印响应头信息
            log.debug("[{}] Response headers: {}", traceId, exchange.getResponse().getHeaders() != null ? exchange.getResponse().getHeaders() : "null");
            
            // 特别打印CORS相关的响应头
            if (exchange.getResponse().getHeaders() != null) {
                log.debug("[{}] CORS headers:", traceId);
                exchange.getResponse().getHeaders().forEach((key, values) -> {
                    if (key.toLowerCase().startsWith("access-control-")) {
                        log.debug("[{}]   {}: {}", traceId, key, values);
                    }
                });
            }
            
            log.info("[{}] RequestLogFilter SUCCESS - {} {} - {} ({}ms) Content-Type: {}", 
                     traceId, method, path, statusCode, duration, contentType);
        }).doOnError(error -> {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] RequestLogFilter ERROR - {} {} - error: {} ({}ms)", traceId, method, path, error.getMessage(), duration);
        });
    }

    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null 
                    ? request.getRemoteAddress().getAddress().getHostAddress() 
                    : "unknown";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return GatewayConstant.FILTER_ORDER_LOG;
    }
}
