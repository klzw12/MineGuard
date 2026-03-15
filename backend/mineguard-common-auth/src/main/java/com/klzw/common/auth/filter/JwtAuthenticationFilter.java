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
 * 支持两种模式：
 * 1. 网关模式：检测到 X-User-Id 请求头时，信任网关传递的用户信息
 * 2. 直连模式：无网关请求头时，正常解析验证 Token
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";

    public JwtAuthenticationFilter(JwtUtils jwtUtils, JwtProperties jwtProperties) {
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String gatewayUserId = request.getHeader(HEADER_USER_ID);
            
            if (gatewayUserId != null) {
                setUserInfoFromHeader(request);
                log.debug("Gateway mode: userId={}", gatewayUserId);
            } else {
                setUserInfoFromToken(request);
            }
            
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
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
            // 支持String类型的userId
            try {
                UserContext.setUserId(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                // 如果userId不是数字，使用hashCode作为Long类型的userId
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
        
        if (token != null) {
            if (jwtUtils.validateToken(token)) {
                // 尝试获取String类型的userId
                String userIdString = jwtUtils.getUserIdStringFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                
                // 设置String类型的userId
                UserContext.setUserIdString(userIdString);
                
                // 尝试转换为Long类型
                try {
                    Long userId = Long.parseLong(userIdString);
                    UserContext.setUserId(userId);
                } catch (NumberFormatException e) {
                    // 如果userId不是数字，使用hashCode作为Long类型的userId
                    UserContext.setUserId((long) userIdString.hashCode());
                }
                
                UserContext.setUsername(username);
                
                // 设置角色
                if (role != null && !role.isEmpty()) {
                    UserContext.setRoles(Arrays.asList(role));
                }
            } else {
                throw new AuthException(AuthResultCode.TOKEN_INVALID);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/login") || path.contains("/register") || path.contains("/captcha") || path.contains("/reset-password");
    }
}
