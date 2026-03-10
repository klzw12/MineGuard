package com.klzw.common.database.config;

import com.klzw.common.database.interceptor.DataSourceClearInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 数据源拦截器配置类
 * <p>
 * 主要功能：
 * 1. 注册数据源清理拦截器
 * 2. 确保ThreadLocal在请求结束后被清理
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication
public class DataSourceInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DataSourceClearInterceptor())
                .addPathPatterns("/**")
                .order(-100);
        
        log.info("数据源清理拦截器注册成功");
    }
}
