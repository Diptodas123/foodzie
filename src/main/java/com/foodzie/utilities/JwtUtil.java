package com.foodzie.utilities;

import com.foodzie.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@ConditionalOnProperty(prefix = "jwt", name = "secret")
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms:3600000}") long accessTokenExpiryMs,       // 1 hour
            @Value("${jwt.refresh-token-expiry-ms:604800000}") long refreshTokenExpiryMs    // 7 days
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String generateAccessToken(UserDTO userDTO) {
        return buildToken(userDTO, accessTokenExpiryMs, "access");
    }

    public String generateRefreshToken(UserDTO userDTO) {
        return buildToken(userDTO, refreshTokenExpiryMs, "refresh");
    }

    private String buildToken(UserDTO userDTO, long expiryMs, String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDTO.getEmail())
                .claim("id", userDTO.getId())
                .claim("role", userDTO.getRole())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public String extractRole(String token) {
        Object role = getClaims(token).get("role");
        return role != null ? role.toString() : "USER";
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
