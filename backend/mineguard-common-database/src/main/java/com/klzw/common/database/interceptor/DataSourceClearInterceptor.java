package com.klzw.common.database.interceptor;

import com.klzw.common.database.datasource.DynamicDataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据源清理拦截器
 * <p>
 * 主要功能：
 * 1. 在请求结束后清理ThreadLocal，防止内存泄漏
 * 2. 确保数据源上下文在请求间隔离
 */
@Slf4j
public class DataSourceClearInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            DynamicDataSource.clearDataSourceContext();
        } catch (Exception e) {
            log.error("清理数据源上下文失败: {}", e.getMessage(), e);
        }
    }
}
