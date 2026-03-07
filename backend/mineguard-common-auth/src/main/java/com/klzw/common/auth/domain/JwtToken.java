package com.klzw.common.auth.domain;

import lombok.Data;

import java.util.Date;

/**
 * JWT Token 实体
 */
@Data
public class JwtToken {

    private String token;
    private Long userId;
    private String username;
    private Date issuedAt;
    private Date expiration;
}
