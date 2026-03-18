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

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    private final GatewayProperties gatewayProperties;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        GatewayProperties.Cors cors = gatewayProperties.getCors();
        
        CorsConfiguration config = new CorsConfiguration();
        
        if (cors.getAllowedOrigins() != null && !cors.getAllowedOrigins().isEmpty()) {
            config.setAllowedOriginPatterns(Arrays.asList(cors.getAllowedOrigins().split(",")));
        } else {
            config.setAllowedOriginPatterns(Arrays.asList("*"));
        }
        
        config.setAllowedMethods(Arrays.asList(cors.getAllowedMethods().split(",")));
        config.setAllowedHeaders(Arrays.asList(cors.getAllowedHeaders().split(",")));
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());
        config.setExposedHeaders(Arrays.asList("X-Trace-Id", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
