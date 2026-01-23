package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.jwt.JwtAuthFilter;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {
        // 1. SPA를 위한 CSRF 핸들러 설정
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Plain text로 된 토큰을 헤더에서 읽을 수 있도록 설정 (기본값 처리)
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .cors(Customizer.withDefaults())

            // 2. CSRF 설정 보강
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/api/v1/signup/naver/complete")
                .ignoringRequestMatchers("/api/v1/signup/password/email/request")
                .ignoringRequestMatchers("/api/v1/signup/password/reset")
            )

            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/hello", "/api/v1/api/test").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/naver/login", "/api/v1/auth/naver/callback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").permitAll()
                .requestMatchers("/api/v1/signup/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        // 3. 필터 순서 설정
        // JWT 필터 먼저 실행
        http.addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        // ✅ 핵심: 매 요청마다 CSRF 토큰을 로드하여 쿠키에 갱신해주는 필터 추가
        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }
}

/**
 * SPA 환경에서 CSRF 토큰을 매 요청마다 로드하여 브라우저 쿠키(XSRF-TOKEN)를 최신화함
 */
final class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // 토큰을 명시적으로 로드하여 응답 쿠키에 쓰이도록 강제함 (Lazy CSRF 해결)
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}