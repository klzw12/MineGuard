package com.klzw.common.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 禁用配置
 * <p>
 * 由于网关已经处理了认证和授权，后端服务不需要再启用 Spring Security 的过滤器链。
 * 此配置类禁用 Spring Security 的默认安全过滤器，允许所有请求通过。
 * <p>
 * 注意：此配置仅禁用 HTTP 安全过滤器，不影响 PasswordEncoder 等加密工具的使用。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
@ConditionalOnProperty(prefix = "mineguard.auth.security", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SecurityDisableConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用默认的注销功能
            .logout(AbstractHttpConfigurer::disable)
            // 允许所有请求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
