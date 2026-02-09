package com.aivle.ai0917.ipai.global.security.jwt;


import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 생성/검증 담당 클래스
 *
 * - access token에 userId(우리 DB PK), role만 넣는 단순 구조
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final JwtProperties props;

    public JwtProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 우리 서비스 Access Token(JWT) 생성 */
    public String createAccessToken(Long userId, UserRole role) {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getAccessTokenExpMinutes() * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId)) // sub = 우리 서비스 userId
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /** Refresh Token(JWT) 생성 */
    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getRefreshTokenExpDays() * 24 * 60 * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")// 권한
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /** 토큰 파싱(서명 검증 + 만료 검증 포함) */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}