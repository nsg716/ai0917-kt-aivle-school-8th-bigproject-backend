package com.aivle.ai0917.ipai.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class PendingSignupTokenProvider {

    private final SecretKey key;
    private final JwtProperties props;

    public PendingSignupTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ✅ 임시 가입 토큰 생성
     * - sub: naverId
     * - typ: PENDING_SIGNUP (구분용)
     * - name/gender/birthday/birthYear/mobile 등 네이버 값 저장
     */
    public String createPendingToken(PendingProfile p, long expMinutes) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);

        return Jwts.builder()
                .subject(p.naverId())
                .claim("typ", "PENDING_SIGNUP")
                .claim("name", p.name())
                .claim("gender", p.gender())
                .claim("birthday", p.birthday())
                .claim("birthYear", p.birthYear())
                .claim("mobile", p.mobile())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public PendingProfile parsePendingToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        if (!"PENDING_SIGNUP".equals(String.valueOf(claims.get("typ")))) {
            throw new RuntimeException("pending 토큰 타입이 아닙니다.");
        }

        return new PendingProfile(
                claims.getSubject(),
                String.valueOf(claims.get("name")),
                String.valueOf(claims.get("gender")),
                String.valueOf(claims.get("birthday")),
                String.valueOf(claims.get("birthYear")),
                String.valueOf(claims.get("mobile"))
        );
    }

    public record PendingProfile(
            String naverId,
            String name,
            String gender,
            String birthday,
            String birthYear,
            String mobile
    ) {}
}
