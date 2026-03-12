package com.klzw.common.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.auth.jwt")
public class JwtProperties {

    private boolean enabled = true;

    private String secret;

    private Long expiration = 86400000L;

    private String header = "Authorization";

    private String prefix = "Bearer ";

    private Boolean enableBlacklist = true;

    private String blacklistPrefix = "jwt:blacklist:";

    private Long blacklistExpire = 86400L;

    private Boolean enableRefresh = true;

    private Long refreshExpiration = 604800000L;
}
