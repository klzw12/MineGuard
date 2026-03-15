package com.klzw.service.gateway;

import com.klzw.common.auth.config.JwtProperties;
import com.klzw.common.auth.util.JwtUtils;
import com.klzw.service.gateway.properties.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.klzw.service.gateway", "com.klzw.common.core"})
@EnableConfigurationProperties({GatewayProperties.class, JwtProperties.class})
public class GatewayTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayTestApplication.class, args);
    }

    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-for-integration-testing-must-be-long-enough");
        properties.setExpiration(3600000L);
        properties.setPrefix("Bearer ");
        properties.setEnableBlacklist(true);
        properties.setBlacklistPrefix("jwt:blacklist:");
        properties.setBlacklistExpire(86400L);
        return properties;
    }

    @Bean
    public JwtUtils jwtUtils(JwtProperties jwtProperties) {
        return new JwtUtils(jwtProperties, null);
    }
}
