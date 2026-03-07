package com.klzw.common.web.config;

import com.klzw.common.web.properties.WebProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 用于配置跨域请求处理
 */
@Configuration
@EnableConfigurationProperties(WebProperties.class)
@RequiredArgsConstructor
public class CorsConfig {
    
    private final WebProperties webProperties;
    
    @Bean
    public CorsFilter corsFilter() {
        WebProperties.Cors cors = webProperties.getCors();
        
        CorsConfiguration config = new CorsConfiguration();
        
        // 只有在配置了allowedOrigins时才添加，避免使用通配符
        if (cors.getAllowedOrigins() != null && !cors.getAllowedOrigins().isEmpty()) {
            config.addAllowedOriginPattern(cors.getAllowedOrigins());
        }
        
        config.addAllowedMethod(cors.getAllowedMethods());
        config.addAllowedHeader(cors.getAllowedHeaders());
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
    
}
