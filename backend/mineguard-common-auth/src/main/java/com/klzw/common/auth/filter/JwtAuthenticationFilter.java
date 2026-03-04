package com.klzw.common.auth.filter;

import com.klzw.common.auth.config.JwtConfig;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.context.UserContext;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.auth.util.JwtUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtConfig jwtConfig;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, JwtConfig jwtConfig) {
        this.jwtUtils = jwtUtils;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(jwtConfig.getHeader());
            String token = jwtUtils.getTokenFromHeader(authHeader);
            
            if (token != null) {
                if (jwtUtils.validateToken(token)) {
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String username = jwtUtils.getUsernameFromToken(token);
                    
                    UserContext.setUserId(userId);
                    UserContext.setUsername(username);
                } else {
                    // Token 无效，抛出异常
                    throw new AuthException(AuthResultCode.TOKEN_INVALID);
                }
            }
            
            filterChain.doFilter(request, response);
        } finally {
            // 清理ThreadLocal，防止内存泄漏
            UserContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/login") || path.contains("/register") || path.contains("/captcha");
    }
}
