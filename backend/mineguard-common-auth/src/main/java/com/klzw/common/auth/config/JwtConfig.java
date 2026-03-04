package com.klzw.common.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置类
 */
@Configuration
@Data
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private final JwtProperties jwtProperties;

    public JwtConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String getSecret() {
        return jwtProperties.getSecret();
    }

    public Long getExpiration() {
        return jwtProperties.getExpiration();
    }

    public String getHeader() {
        return jwtProperties.getHeader();
    }

    public String getPrefix() {
        return jwtProperties.getPrefix();
    }

    public Boolean getEnableBlacklist() {
        return jwtProperties.getEnableBlacklist();
    }

    public String getBlacklistPrefix() {
        return jwtProperties.getBlacklistPrefix();
    }

    public Long getBlacklistExpire() {
        return jwtProperties.getBlacklistExpire();
    }

    public Boolean getEnableRefresh() {
        return jwtProperties.getEnableRefresh();
    }

    public Long getRefreshExpiration() {
        return jwtProperties.getRefreshExpiration();
    }
}
