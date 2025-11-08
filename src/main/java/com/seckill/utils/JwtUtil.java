package com.seckill.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JwtUtil {
    //1.设置Token有效期
    public static final long EXPIRE = 1000 * 60 * 60 * 24;
    //2.设置Token密钥 (HS256需要至少256位/32字符的密钥)
    public static final String TOKEN_SECRET = "secret_seckill_order_token_1234567890abcdef";
    
    /**
     * JWT签名密钥，用于Token的加密和解密
     * 根据TOKEN_SECRET字符串生成HS256算法所需的SecretKey对象
     */
    private static final SecretKey SECRET_KEY = new SecretKeySpec(TOKEN_SECRET.getBytes(), SignatureAlgorithm.HS256.getJcaName());

    //3.生成Token
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    //4解析Token
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new SignatureException("Token 解析错误");
        }
    }
    //5.从Token中获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

}