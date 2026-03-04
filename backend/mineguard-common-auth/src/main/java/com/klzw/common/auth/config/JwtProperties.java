package com.klzw.common.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret = "defaultSecretKeyForJwtTokenGeneration12345678901234567890";

    /**
     * JWT过期时间（毫秒）
     */
    private Long expiration = 86400000L;

    /**
     * JWT请求头名称
     */
    private String header = "Authorization";

    /**
     * JWT令牌前缀
     */
    private String prefix = "Bearer ";

    /**
     * 是否启用JWT黑名单功能
     */
    private Boolean enableBlacklist = true;

    /**
     * 黑名单缓存键前缀
     */
    private String blacklistPrefix = "jwt:blacklist:";

    /**
     * 黑名单缓存过期时间（秒）
     */
    private Long blacklistExpire = 86400L;

    /**
     * 是否启用JWT刷新令牌功能
     */
    private Boolean enableRefresh = true;

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private Long refreshExpiration = 604800000L;
}
