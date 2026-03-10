package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtProperties;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.domain.JwtToken;
import com.klzw.common.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;

    public JwtUtils(JwtProperties jwtProperties, StringRedisTemplate redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims, username);
    }

    private String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

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

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtProperties.getPrefix())) {
            return authHeader.substring(jwtProperties.getPrefix().length());
        }
        return null;
    }

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

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            if (jwtProperties.getEnableBlacklist() && isInBlacklist(token)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addToBlacklist(String token) {
        if (jwtProperties.getEnableBlacklist()) {
            String key = jwtProperties.getBlacklistPrefix() + token;
            redisTemplate.opsForValue().set(key, "1", jwtProperties.getBlacklistExpire(), TimeUnit.SECONDS);
        }
    }

    public boolean isInBlacklist(String token) {
        if (!jwtProperties.getEnableBlacklist()) {
            return false;
        }
        String key = jwtProperties.getBlacklistPrefix() + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }
}
