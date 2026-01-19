package com.ddl.manager.infrastructure.config;

import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {
    // 密钥（建议配置在 application.yml 中）
    @Value("${jwt.secret:your-secret-key}")
    private String secret;

    // 过期时间（例如 2 小时）
    @Getter
    @Value("${jwt.expiration:7200000}")
    private long expiration;

    // 生成 Token
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // 用用户名作为 Token 主题
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(SignatureAlgorithm.HS512, secret) // 签名算法和密钥
                .compact();
    }

    // 从 Token 中获取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // 检查 Token 是否过期
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

}