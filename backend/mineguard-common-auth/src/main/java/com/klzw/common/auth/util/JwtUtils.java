package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtConfig;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.domain.JwtToken;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.redis.service.RedisCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类
 */
@Component
public class JwtUtils {

    private final JwtConfig jwtConfig;
    private final RedisCacheService redisCacheService;

    public JwtUtils(JwtConfig jwtConfig, RedisCacheService redisCacheService) {
        this.jwtConfig = jwtConfig;
        this.redisCacheService = redisCacheService;
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @return Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims, username);
    }

    /**
     * 生成 Token
     * @param claims 声明
     * @param subject 主题
     * @return Token
     */
    private String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 解析 Token
     * @param token Token
     * @return 声明
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new AuthException(AuthResultCode.TOKEN_EXPIRED, e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new AuthException(AuthResultCode.TOKEN_SIGNATURE_ERROR, e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, e);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, e);
        } catch (io.jsonwebtoken.JwtException e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, e);
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.TOKEN_PARSE_ERROR, e);
        }
    }

    /**
     * 从 Token 中获取用户ID
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     * @param token Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }


    /**
     * 从请求头中获取 Token
     * @param authHeader 认证头
     * @return Token
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtConfig.getPrefix())) {
            return authHeader.substring(jwtConfig.getPrefix().length());
        }
        return null;
    }

    /**
     * 获取 Token 信息
     * @param token Token
     * @return Token 信息
     */
    public JwtToken getTokenInfo(String token) {
        Claims claims = parseToken(token);
        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(token);
        jwtToken.setUserId(claims.get("userId", Long.class));
        jwtToken.setUsername(claims.getSubject());
        jwtToken.setIssuedAt(claims.getIssuedAt());
        jwtToken.setExpiration(claims.getExpiration());
        return jwtToken;
    }

    /**
     * 生成token黑名单key
     * @param token Token
     * @return 黑名单key
     */
    private String generateBlacklistKey(String token) {
        return "auth:token:blacklist:" + token;
    }

    /**
     * 将token加入黑名单
     * @param token Token
     */
    public void addToBlacklist(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long expireTime = expiration.getTime() - System.currentTimeMillis();
            
            if (expireTime > 0) {
                String blacklistKey = generateBlacklistKey(token);
                redisCacheService.set(blacklistKey, "1", expireTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 忽略解析异常，确保即使token无效也能处理
        }
    }

    /**
     * 检查token是否在黑名单中
     * @param token Token
     * @return 是否在黑名单中
     */
    public boolean isInBlacklist(String token) {
        try {
            String blacklistKey = generateBlacklistKey(token);
            return redisCacheService.exists(blacklistKey);
        } catch (Exception e) {
            // 发生异常时默认返回false，避免Redis故障影响正常认证
            return false;
        }
    }

    /**
     * 验证 Token
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 检查token是否在黑名单中
            if (isInBlacklist(token)) {
                return false;
            }
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
