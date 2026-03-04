package com.klzw.common.web.config;

import com.klzw.common.auth.filter.JwtAuthenticationFilter;
import com.klzw.common.web.interceptor.AuthInterceptor;
import com.klzw.common.web.resolver.CurrentUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类
 * 用于配置 Web MVC 相关的功能
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Bean
    @ConditionalOnBean(JwtAuthenticationFilter.class)
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .order(2);
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}
