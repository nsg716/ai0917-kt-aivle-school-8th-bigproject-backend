package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.jwt.JwtAuthFilter;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {

        http
                // CORS는 WebMvcConfigurer 설정을 따르도록 켜주는 게 안전
                .cors(Customizer.withDefaults())

                // ✅ HttpOnly 쿠키 인증이면 CSRF 켜는 것을 권장
                // 프론트는 XSRF-TOKEN 쿠키를 읽어서 X-XSRF-TOKEN 헤더로 보내면 됨
                .csrf(csrf -> csrf
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // OAuth 콜백은 GET이라 보통 CSRF 영향 없음.
                        // 필요하면 특정 경로만 ignore도 가능 (원하면 아래처럼)
                        // .ignoringRequestMatchers("/api/v1/auth/naver/**")
                        .ignoringRequestMatchers("/api/v1/signup/naver/complete")
                )

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 공개 API만 정확히 오픈
                        .requestMatchers(
                                "/api/v1/hello",
                                "/api/v1/api/test"
                        ).permitAll()

                        // 네이버 OAuth 시작/콜백은 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/auth/naver/login",
                                "/api/v1/auth/naver/callback"
                        ).permitAll()

                        // signup 진행(이메일 인증/가입완료)은 공개지만 CSRF 토큰은 요구될 수 있음
                        .requestMatchers("/api/v1/signup/**").permitAll()

                        // 네가 확정한 auth/me 도 공개로 둘지, 인증 필요로 둘지 정책 선택
                        // - pendingSignup 용도면 공개 OK (쿠키 없으면 에러)
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http.addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
