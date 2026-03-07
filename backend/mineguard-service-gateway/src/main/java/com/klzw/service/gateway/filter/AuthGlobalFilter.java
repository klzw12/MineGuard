package com.klzw.service.gateway.filter;

import com.klzw.common.auth.util.JwtUtils;
import com.klzw.service.gateway.constant.GatewayConstant;
import com.klzw.service.gateway.properties.GatewayProperties;
import com.klzw.service.gateway.util.ResponseUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

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
        
        if (gatewayProperties.getIgnoreAuth().isIgnored(path)) {
            log.debug("Path {} is ignored for auth", path);
            return chain.filter(exchange);
        }
        
        String authHeader = request.getHeaders().getFirst(GatewayConstant.AUTHORIZATION_HEADER);
        String token = getTokenFromHeader(authHeader);
        
        if (token == null) {
            log.warn("Token missing for path: {}", path);
            return ResponseUtils.unauthorized(exchange.getResponse(), "Token缺失");
        }
        
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (Exception e) {
            log.warn("Token invalid for path: {}, error: {}", path, e.getMessage());
            return ResponseUtils.unauthorized(exchange.getResponse(), "Token无效或已过期");
        }
        
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        List<String> roles = jwtUtils.getRolesFromToken(token);
        
        if (userId == null || username == null) {
            log.warn("Token missing user info for path: {}", path);
            return ResponseUtils.unauthorized(exchange.getResponse(), "Token信息不完整");
        }
        
        ServerHttpRequest newRequest = request.mutate()
                .header(GatewayConstant.HEADER_USER_ID, String.valueOf(userId))
                .header(GatewayConstant.HEADER_USERNAME, username)
                .header(GatewayConstant.HEADER_ROLES, roles != null ? String.join(",", roles) : "")
                .build();
        
        log.debug("Auth success: userId={}, username={}", userId, username);
        
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private String getTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(GatewayConstant.BEARER_PREFIX)) {
            return authHeader.substring(GatewayConstant.BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    public int getOrder() {
        return GatewayConstant.FILTER_ORDER_AUTH;
    }
}
