package com.klzw.common.auth.filter;

import com.klzw.common.auth.config.JwtProperties;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.context.UserContext;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * <p>
 * 支持三种模式：
 * 1. 网关模式：检测到 X-User-Id 请求头时，信任网关传递的用户信息
 * 2. 服务间调用模式：检测到 X-Service-Auth 请求头时，跳过认证（服务间内部调用）
 * 3. 直连模式：无网关请求头时，正常解析验证 Token
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_SERVICE_AUTH = "X-Service-Auth";
    private static final String SERVICE_AUTH_TOKEN = "mineguard-internal-service";

    public JwtAuthenticationFilter(JwtUtils jwtUtils, JwtProperties jwtProperties) {
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String gatewayUserId = request.getHeader(HEADER_USER_ID);
            String serviceAuth = request.getHeader(HEADER_SERVICE_AUTH);
            
            if (gatewayUserId != null) {
                setUserInfoFromHeader(request);
                log.debug("Gateway mode: userId={}", gatewayUserId);
            } else if (isServiceAuth(serviceAuth)) {
                log.debug("Service-to-service mode: path={}", request.getRequestURI());
            } else {
                setUserInfoFromToken(request);
            }
            
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isServiceAuth(String serviceAuth) {
        return SERVICE_AUTH_TOKEN.equals(serviceAuth);
    }

    /**
     * 从网关请求头读取用户信息
     * @param request HTTP请求
     */
    private void setUserInfoFromHeader(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String roles = request.getHeader(HEADER_ROLES);
        
        if (userId != null) {
            try {
                UserContext.setUserId(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                UserContext.setUserId((long) userId.hashCode());
            }
            UserContext.setUserIdString(userId);
            UserContext.setUsername(username);
            
            if (StringUtils.hasText(roles)) {
                List<String> roleList = Arrays.asList(roles.split(","));
                UserContext.setRoles(roleList);
            }
        }
    }

    /**
     * 从Token解析用户信息
     * @param request HTTP请求
     */
    private void setUserInfoFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtProperties.getHeader());
        String token = jwtUtils.getTokenFromHeader(authHeader);
        
        if (token == null) {
            log.debug("No token found in request, skipping authentication");
            return;
        }
        
        if (jwtUtils.validateToken(token)) {
            String userIdString = jwtUtils.getUserIdStringFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            
            UserContext.setUserIdString(userIdString);
            
            try {
                Long userId = Long.parseLong(userIdString);
                UserContext.setUserId(userId);
            } catch (NumberFormatException e) {
                UserContext.setUserId((long) userIdString.hashCode());
            }
            
            UserContext.setUsername(username);
            
            if (role != null && !role.isEmpty()) {
                UserContext.setRoles(Arrays.asList(role));
            }
        } else {
            throw new AuthException(AuthResultCode.TOKEN_INVALID);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter checking path: {}", path);
        boolean shouldNotFilter = path.contains("/login") || path.contains("/register") || path.contains("/captcha") || path.contains("/reset-password") || path.contains("/auth/refresh") || path.startsWith("/ws/");
        log.debug("JwtAuthenticationFilter shouldNotFilter: {}", shouldNotFilter);
        return shouldNotFilter;
    }
}