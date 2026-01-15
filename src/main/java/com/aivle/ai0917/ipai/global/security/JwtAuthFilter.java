package com.aivle.ai0917.ipai.global.security;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 모든 요청에서 Authorization 헤더의 Bearer 토큰을 확인하여 인증 처리
 *
 * - 토큰이 유효하면:
 *   SecurityContext에 Authentication을 넣어서
 *   컨트롤러에서 @AuthenticationPrincipal 또는 Authentication으로 userId를 꺼낼 수 있음
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer 형식이 아니면 인증 없이 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            Claims claims = jwtProvider.parse(token);

            Long userId = Long.valueOf(claims.getSubject());
            String role = String.valueOf(claims.get("role"));

            // Spring Security 권한 리스트 구성
            var authorities = List.of(new SimpleGrantedAuthority(role));

            // principal에는 userId(Long)만 넣어서 단순하게 사용
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // SecurityContext에 저장(= 인증 완료)
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 만료/위조/파싱오류 등 발생 시 인증 제거하고 통과
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}