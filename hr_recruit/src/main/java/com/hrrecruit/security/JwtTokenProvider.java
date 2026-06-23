package com.hrrecruit.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 * 负责 Token 的生成和解析
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** 从 application.yml 读取密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** 从 application.yml 读取过期时间 */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥（将字符串转为 SecretKey 对象）
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)                    // 主题：用户名
                .claim("userId", userId)              // 自定义声明：用户ID
                .issuedAt(now)                        // 签发时间
                .expiration(expiryDate)               // 过期时间
                .signWith(getSigningKey())            // 签名
                .compact();
    }

    /**
     * 从 Token 中解析用户名
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 从 Token 中解析用户ID
     */
    public Long getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT Token 验证失败: {}", e.getMessage());
            return false;
        }
    }
}
