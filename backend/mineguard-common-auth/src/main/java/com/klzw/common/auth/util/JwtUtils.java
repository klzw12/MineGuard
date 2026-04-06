package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtProperties;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.domain.JwtToken;
import com.klzw.common.auth.enums.RoleEnum;
import com.klzw.common.auth.exception.AuthException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    private JWSSigner getSigner() {
        try {
            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            return new MACSigner(keyBytes);
        } catch (JOSEException e) {
            throw new AuthException(AuthResultCode.AUTH_ERROR, "JWT签名器创建失败", e);
        }
    }

    private JWSVerifier getVerifier() {
        try {
            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            return new MACVerifier(keyBytes);
        } catch (JOSEException e) {
            throw new AuthException(AuthResultCode.AUTH_ERROR, "JWT验证器创建失败", e);
        }
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (role != null && !role.isEmpty()) {
            claims.put("role", role);
        }
        return generateToken(claims, username);
    }
    
    public String generateToken(String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (role != null && !role.isEmpty()) {
            claims.put("role", role);
        }
        return generateToken(claims, username);
    }
    
    public String generateToken(Long userId, String username, RoleEnum role) {
        return generateToken(userId, username, role != null ? role.getValue() : null);
    }
    
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, (String) null);
    }
    
    public String generateToken(String userId, String username) {
        return generateToken(userId, username, (String) null);
    }
    
    public String generateRefreshToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        return generateToken(claims, username + ":refresh");
    }

    private String generateToken(Map<String, Object> claims, String subject) {
        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration());

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(now)
                    .expirationTime(expirationDate);

            // 添加自定义声明
            claims.forEach(claimsBuilder::claim);

            JWTClaimsSet claimsSet = claimsBuilder.build();
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            signedJWT.sign(getSigner());
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new AuthException(AuthResultCode.AUTH_ERROR, "Token生成失败", e);
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // 验证签名
            if (!signedJWT.verify(getVerifier())) {
                throw new AuthException(AuthResultCode.TOKEN_SIGNATURE_ERROR, "Token签名验证失败");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // 验证过期时间
            if (claimsSet.getExpirationTime().before(new Date())) {
                throw new AuthException(AuthResultCode.TOKEN_EXPIRED, "Token已过期");
            }

            return claimsSet;
        } catch (AuthException e) {
            throw e;
        } catch (JOSEException e) {
            throw new AuthException(AuthResultCode.TOKEN_SIGNATURE_ERROR, "Token签名错误", e);
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "Token无效", e);
        }
    }

    /**
     * 解析Token但不验证过期时间（用于刷新令牌）
     */
    public JWTClaimsSet parseTokenWithoutExpiration(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // 验证签名
            if (!signedJWT.verify(getVerifier())) {
                throw new AuthException(AuthResultCode.TOKEN_SIGNATURE_ERROR, "Token签名验证失败");
            }

            return signedJWT.getJWTClaimsSet();
        } catch (AuthException e) {
            throw e;
        } catch (JOSEException e) {
            throw new AuthException(AuthResultCode.TOKEN_SIGNATURE_ERROR, "Token签名错误", e);
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "Token无效", e);
        }
    }

    public Long getUserIdFromToken(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        Object userIdObject = claimsSet.getClaim("userId");
        if (userIdObject == null) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "用户ID缺失");
        }
        if (userIdObject instanceof Number) {
            return ((Number) userIdObject).longValue();
        }
        throw new AuthException(AuthResultCode.TOKEN_INVALID, "用户ID格式错误");
    }
    
    public String getUserIdStringFromToken(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        Object userIdObject = claimsSet.getClaim("userId");
        if (userIdObject == null) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "用户ID缺失");
        }
        return userIdObject.toString();
    }

    public String getUsernameFromToken(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        return claimsSet.getSubject();
    }
    
    public String getRoleFromToken(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        try {
            return claimsSet.getStringClaim("role");
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.TOKEN_INVALID, "Token无效", e);
        }
    }

    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtProperties.getPrefix())) {
            return authHeader.substring(jwtProperties.getPrefix().length());
        }
        return null;
    }

    public JwtToken getTokenInfo(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(token);
        jwtToken.setUserId(getUserIdFromToken(token));
        jwtToken.setUsername(claimsSet.getSubject());
        jwtToken.setRole(getRoleFromToken(token));
        jwtToken.setIssuedAt(claimsSet.getIssueTime());
        jwtToken.setExpiration(claimsSet.getExpirationTime());
        return jwtToken;
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !jwtProperties.getEnableBlacklist() || !isInBlacklist(token);
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
