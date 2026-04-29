package org.example.ordermanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 提供 JWT 的生成、解析、验证等核心操作。它会在全局认证过滤器处理请求时协助验证 Authorization 头，
 * 并支持从中提取特定身份信息（如用户名和角色标志）。
 * </p>
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 根据密钥获取签名用的 Key 对象
     *
     * @return 用于 HMAC-SHA256 算法的 Key 实例
     */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成正常登录用户的 JWT Token
     *
     * @param username 用户名
     * @param role     用户角色（如 USER, ADMIN, MERCHANT）
     * @return 构建并签名的 JWT 字符串
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成游客 Token：有效期 2 小时，role=GUEST，不关联任何数据库用户
     */
    public String generateGuestToken() {
        String guestSubject = "guest_" + System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(guestSubject)
                .claim("role", "GUEST")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000L))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT Token，提取内部的载荷 (Claims) 信息
     *
     * @param token 需要解析的 JWT 字符串
     * @return 包含对应 Token 信息的 Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户名 (Subject)
     *
     * @param token JWT 字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取角色信息
     *
     * @param token JWT 字符串
     * @return 角色字符串
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 校验 Token 是否有效且未过期
     *
     * @param token JWT 字符串
     * @return 格式合法并且未过期时返回 true，否则返回 false
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 HTTP 请求头中提取并截取 Bearer Token
     *
     * @param request HTTP 请求对象
     * @return 取出的 Token 纯字符串，若无则返回 null
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 从 HTTP 请求中提取关联的用户 ID
     * (需配合实际业务 UserService 转换用户名到特定数据库 ID)
     *
     * @param request HTTP 请求对象
     * @return 用户 ID，获取失败或无用户信息时返回 null
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            String username = getUsername(token);
            // 这里需要通过用户名获取用户ID，实际使用时注入UserService
            // 暂时返回null，由调用方处理
            return null;
        }
        return null;
    }
}