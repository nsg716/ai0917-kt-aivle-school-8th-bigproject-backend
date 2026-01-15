package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.jwt.JwtAuthFilter;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 프론트 분리(React 등) + 백엔드 API 서버라면:
 * - formLogin(서버 렌더링) X
 * - 세션 X (stateless)
 * - JWT 인증 필터로 보호
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {
        http
                // API 서버에서는 보통 CSRF 비활성화(세션 쿠키 기반이 아닐 경우)
                .csrf(csrf -> csrf.disable())

                // 세션을 만들지 않음(매 요청 JWT로 인증)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 네이버 로그인 시작/콜백은 누구나 접근 가능해야 함
                        .requestMatchers(
                                "/api/v1/hello",
                                "/api/v1/auth/naver/login",
                                "/api/v1/auth/naver/callback",
                                "/api/v1/auth/naver/hello",
                                "/api/v1/login",
                                "/api/v1/api/test"
                        ).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // formLogin 비활성화(프론트 분리형에서는 사용 안 함)
                .formLogin(form -> form.disable());

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 넣어서
        // 토큰이 있으면 먼저 인증이 잡히도록 함
        http.addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}