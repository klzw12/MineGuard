package com.klzw.service.gateway.config;

import com.klzw.service.gateway.properties.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@RequiredArgsConstructor
@Slf4j
public class CorsConfig {

    private final GatewayProperties gatewayProperties;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        GatewayProperties.Cors cors = gatewayProperties.getCors();
        
        log.info("=== CORS Configuration Initialization ===");
        log.info("CORS configuration from properties: allowedOrigins={}, allowedMethods={}, allowedHeaders={}, allowCredentials={}, maxAge={}", 
                 cors.getAllowedOrigins(), cors.getAllowedMethods(), cors.getAllowedHeaders(), 
                 cors.isAllowCredentials(), cors.getMaxAge());
        
        CorsConfiguration config = new CorsConfiguration();
        
        // 使用配置文件中的允许源
        if (cors.getAllowedOrigins() != null && !cors.getAllowedOrigins().isEmpty()) {
            String[] origins = cors.getAllowedOrigins().split(",");
            config.setAllowedOrigins(Arrays.asList(origins));
            log.info("Using configured allowed origins: {}", config.getAllowedOrigins());
        } else {
            // 默认只允许本地开发环境
            config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
            log.info("Using default allowed origin: http://localhost:5173");
        }
        
        String[] methods = cors.getAllowedMethods().split(",");
        config.setAllowedMethods(Arrays.asList(methods));
        log.info("Allowed methods: {}", config.getAllowedMethods());
        
        String[] headers = cors.getAllowedHeaders().split(",");
        config.setAllowedHeaders(Arrays.asList(headers));
        log.info("Allowed headers: {}", config.getAllowedHeaders());
        
        config.setAllowCredentials(cors.isAllowCredentials());
        log.info("Allow credentials: {}", config.getAllowCredentials());
        
        config.setMaxAge(cors.getMaxAge());
        log.info("Max age: {}", config.getMaxAge());
        
        config.setExposedHeaders(Arrays.asList("X-Trace-Id", "Authorization"));
        log.info("Exposed headers: {}", config.getExposedHeaders());

        log.info("=== Final CORS Configuration ===");
        log.info("allowedOrigins={}, allowedMethods={}, allowedHeaders={}, allowCredentials={}, maxAge={}, exposedHeaders={}", 
                 config.getAllowedOrigins(), config.getAllowedMethods(), config.getAllowedHeaders(), 
                 config.getAllowCredentials(), config.getMaxAge(), config.getExposedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        log.info("CORS filter registered for all paths with highest precedence");
        
        // 创建CorsWebFilter并添加日志
        CorsWebFilter corsWebFilter = new CorsWebFilter(source);
        log.info("CorsWebFilter created successfully: {}", corsWebFilter.getClass().getName());
        
        return corsWebFilter;
    }
}
