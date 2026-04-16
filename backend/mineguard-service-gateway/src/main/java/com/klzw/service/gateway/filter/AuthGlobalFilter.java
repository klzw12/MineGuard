package com.klzw.service.gateway.filter;

import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.service.gateway.constant.GatewayConstant;
import com.klzw.service.gateway.properties.GatewayProperties;
import com.klzw.service.gateway.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayProperties gatewayProperties;
    private final JwtUtils jwtUtils;

    public AuthGlobalFilter(GatewayProperties gatewayProperties, JwtUtils jwtUtils) {
        this.gatewayProperties = gatewayProperties;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        log.debug("AuthGlobalFilter processing path: {}, method: {}, URI: {}", path, request.getMethod(), request.getURI());
        
        // 处理OPTIONS请求（CORS预检）
        if ("OPTIONS".equals(request.getMethod().name())) {
            log.debug("OPTIONS request detected, passing to next filter", path);
            return chain.filter(exchange);
        }
        
        if (gatewayProperties.getIgnoreAuth().isIgnored(path)) {
            log.debug("Path {} is ignored for auth, passing to next filter", path);
            log.debug("当前响应状态吗：{}", exchange.getResponse().getStatusCode());
            return chain.filter(exchange);
        }
        
        log.debug("Path {} requires auth, checking token", path);
        
        // 从请求头中获取token
        String authHeader = request.getHeaders().getFirst(GatewayConstant.AUTHORIZATION_HEADER);
        log.debug("Auth header: {}", authHeader != null ? "present" : "null");
        
        String token = getTokenFromHeader(authHeader);
        
        // 如果请求头中没有token，从查询参数中获取
        if (token == null) {
            token = getTokenFromQueryParams(request);
            if (token != null) {
                log.debug("Token found in query parameters");
            }
        }
        
        if (token == null) {
            log.warn("Token missing for path: {}", path);
            return ResponseUtils.unauthorized(exchange.getResponse(), "Token缺失");
        }
        
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token invalid or in blacklist for path: {}", path);
            return ResponseUtils.unauthorized(exchange.getResponse(), "Token无效或已过期");
        }
        
        Long userId;
        String username; 
        String role;
        
        try {
            userId = jwtUtils.getUserIdFromToken(token);
            username = jwtUtils.getUsernameFromToken(token);
            role = jwtUtils.getRoleFromToken(token);
        } catch (AuthException e) {
            log.warn("Token invalid for path: {}, error: {}", path, e.getMessage());
            return ResponseUtils.unauthorized(exchange.getResponse(), e.getMessage());
        }
        
        ServerHttpRequest newRequest = request.mutate()
                .header(GatewayConstant.HEADER_USER_ID, String.valueOf(userId))
                .header(GatewayConstant.HEADER_USERNAME, username)
                .header(GatewayConstant.HEADER_ROLES, role != null ? role : "")
                .build();
        
        log.debug("Auth success: userId={}, username={}, routing to: {}", userId, username, newRequest.getURI());
        
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private String getTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(GatewayConstant.BEARER_PREFIX)) {
            return authHeader.substring(GatewayConstant.BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 从查询参数中获取token
     * @param request HTTP请求
     * @return token字符串
     */
    private String getTokenFromQueryParams(ServerHttpRequest request) {
        return request.getQueryParams().getFirst("token");
    }

    @Override
    public int getOrder() {
        return GatewayConstant.FILTER_ORDER_AUTH;
    }
}
