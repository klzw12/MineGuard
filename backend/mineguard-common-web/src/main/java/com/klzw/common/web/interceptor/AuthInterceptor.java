package com.klzw.common.web.interceptor;

import com.klzw.common.auth.annotation.IgnoreAuth;
import com.klzw.common.auth.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * 用于处理用户认证信息
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_SERVICE_AUTH = "X-Service-Auth";
    private static final String SERVICE_AUTH_TOKEN = "mineguard-internal-service";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        IgnoreAuth ignoreAuth = handlerMethod.getMethodAnnotation(IgnoreAuth.class);
        
        if (ignoreAuth != null || handlerMethod.getBeanType().getAnnotation(IgnoreAuth.class) != null) {
            return true;
        }

        // 检查服务间调用认证
        String serviceAuth = request.getHeader(HEADER_SERVICE_AUTH);
        if (SERVICE_AUTH_TOKEN.equals(serviceAuth)) {
            log.debug("Service-to-service mode: path={}", request.getRequestURI());
            return true;
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            // 尝试从请求头中读取用户信息（网关模式）
            String gatewayUserId = request.getHeader(HEADER_USER_ID);
            if (gatewayUserId != null) {
                String username = request.getHeader(HEADER_USERNAME);
                String roles = request.getHeader(HEADER_ROLES);
                
                // 设置用户信息到UserContext
                try {
                    UserContext.setUserId(Long.parseLong(gatewayUserId));
                } catch (NumberFormatException e) {
                    UserContext.setUserId((long) gatewayUserId.hashCode());
                }
                UserContext.setUserIdString(gatewayUserId);
                UserContext.setUsername(username);
                
                if (org.springframework.util.StringUtils.hasText(roles)) {
                    UserContext.setRoles(java.util.Arrays.asList(roles.split(",")));
                }
                
                log.debug("Gateway mode: userId={}, username={}", gatewayUserId, username);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户未登录\"}");
                return false;
            }
        }

        log.debug("AuthInterceptor preHandle: userId={}, url={}", UserContext.getUserId(), request.getRequestURL());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("AuthInterceptor postHandle: {}", request.getRequestURL());
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 注意：UserContext.clear()已在JwtAuthenticationFilter中处理，避免重复清理
        log.debug("AuthInterceptor afterCompletion: {}", request.getRequestURL());
    }
}
