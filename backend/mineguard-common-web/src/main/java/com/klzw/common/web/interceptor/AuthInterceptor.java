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

        Long userId = UserContext.getUserId();
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"用户未登录\"}");
            return false;
        }

        log.info("AuthInterceptor preHandle: userId={}, url={}", userId, request.getRequestURL());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("AuthInterceptor postHandle: {}", request.getRequestURL());
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 注意：UserContext.clear()已在JwtAuthenticationFilter中处理，避免重复清理
        log.info("AuthInterceptor afterCompletion: {}", request.getRequestURL());
    }
}
