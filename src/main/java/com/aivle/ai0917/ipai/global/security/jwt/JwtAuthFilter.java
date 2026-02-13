package com.aivle.ai0917.ipai.global.security.jwt;

import com.aivle.ai0917.ipai.global.security.token.TokenBlacklistService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String ACCESS_COOKIE = "accessToken";
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;


    public JwtAuthFilter(JwtProvider jwtProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        String token = resolveFromCookie(request);
        boolean tokenPresent = (token != null && !token.isBlank());

        // ✅ 1) 요청 + 토큰 존재 여부 로그
        log.info("[JwtAuthFilter] {} {} tokenPresent={}", method, uri, tokenPresent);

        // ✅ 토큰이 없으면 인증 없이 통과
        if (!tokenPresent) {
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenBlacklistService.isBlacklisted(token)) {
            SecurityContextHolder.clearContext();
            log.warn("[JwtAuthFilter] token is blacklisted");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtProvider.parse(token);

            Long userId = Long.valueOf(claims.getSubject());
            String roleClaim = String.valueOf(claims.get("role"));

            // ✅ 2) 파싱 성공 로그
            log.info("[JwtAuthFilter] parsed userId={}, roleClaim={}", userId, roleClaim);

            // ✅ 3) Spring Security 관례: ROLE_ 접두어 통일
            // - 토큰에 "Author" 들어있으면 -> "ROLE_Author"로 세팅
            String authority = roleClaim.startsWith("ROLE_") ? roleClaim : "ROLE_" + roleClaim;

            var authorities = List.of(new SimpleGrantedAuthority(authority));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[JwtAuthFilter] auth set ok, authority={}", authority);

        } catch (Exception e) {
            // 만료/위조 등 -> 인증 제거
            SecurityContextHolder.clearContext();
            log.warn("[JwtAuthFilter] token invalid/expired. cause={}", e.toString());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (ACCESS_COOKIE.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
