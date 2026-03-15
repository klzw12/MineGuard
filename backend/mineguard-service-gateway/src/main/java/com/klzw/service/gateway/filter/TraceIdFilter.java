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

import java.util.UUID;

/**
 * 链路追踪过滤器
 */
@Slf4j
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(GatewayConstant.HEADER_TRACE_ID);
        final String finalTraceId;
        
        if (traceId == null || traceId.isEmpty()) {
            finalTraceId = UUID.randomUUID().toString().replace("-", "");
        } else {
            finalTraceId = traceId;
        }
        
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(GatewayConstant.HEADER_TRACE_ID, finalTraceId)
                .build();
        
        if (exchange.getResponse() != null) {
            exchange.getResponse().getHeaders().add(GatewayConstant.HEADER_TRACE_ID, finalTraceId);
        }
        
        log.debug("TraceId: {}", finalTraceId);
        
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return GatewayConstant.FILTER_ORDER_TRACE;
    }
}
