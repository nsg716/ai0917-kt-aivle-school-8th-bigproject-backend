package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.jwt.JwtAuthFilter;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {

        http
                // ✅ Security 레벨 CORS
                .cors(cors -> {})

                // ✅ CSRF ON (쿠키 저장소) + ✅ "원본 토큰" 방식으로 처리
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()) // ⭐ 핵심!
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        // ❌ /api/v1/csrf 는 ignore 하지 말자 (GET은 원래 검사 안 함)


                )

                // ✅ 세션 안 씀
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // ✅ Preflight는 항상 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // CSRF 토큰 발급 엔드포인트 공개
                        .requestMatchers(HttpMethod.GET, "/api/v1/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/csrf/refresh").permitAll()

                        // 로그아웃 공개 (CSRF는 여기서도 필요하면 프론트가 보내면 됨)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/v1/hello",
                                "/api/v1/ai/**",
                                "/api/v1/auth/naver/**",
                                "/api/v1/signup/**",
                                "/error",
                                "/",
                                "/api/v1/hello",
                                "/api/v1/ai/**",
                                "/api/v1/api/test",

                                "/api/v1/auth/naver/hello",
                                "/api/v1/auth/naver/user",
                                "/api/v1/auth/login",
                                "/api/v1/api/test",

                                "/api/v1/admin/sysnotice/**",
                                "/api/v1/notice/**",
                                "/api/v1/admin/dashboard/**",
                                "/api/v1/admin/access/**",

                                "/api/v1/author/dashboard/**",
                                "/api/v1/author/manuscript/**",
                                "/api/v1/author/**",

                                "/api/v1/manager/iptrend/**",

                                "/api/v1/author/**",
                                "/error",
                                "/api/v1/author/manager/**",
                                "/api/v1/manager/**",
                                "/api/v1/manager/dashboard/**",
                                "/api/v1/manager/ipext/**",

                                "/api/v1/author/works/**",
                                "/api/v1/signup/naver/complete",
                                "/api/v1/admin/sysnotice/**",
                                "/api/v1/**",
                                "/api/v1/auth/naver/login",
                                "/api/v1/auth/naver/callback",
                                "/api/v1/auth/me",
                                "/api/v1/signup/**"

                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

//        http.addFilterAfter(new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(
//                    HttpServletRequest request,
//                    HttpServletResponse response,
//                    FilterChain filterChain
//            ) throws ServletException, IOException {
//                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//                if (csrfToken != null) {
//                    csrfToken.getToken();
//                }
//                filterChain.doFilter(request, response);
//            }
//        }, CsrfFilter.class);

        // ✅ JWT 필터 (CSRF보다 먼저 돌게 하고 싶으면 CsrfFilter 앞에 둬도 됨)
        // 보통은 CSRF와 무관하지만, 확실히 하려면 아래처럼 CsrfFilter 앞에 둬도 OK
        http.addFilterBefore(new JwtAuthFilter(jwtProvider), CsrfFilter.class);
        // 또는 기존처럼:
        // http.addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CSRF 쿠키/헤더 이름 & Path 고정
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookiePath("/");              // ✅ 핵심 (경로 꼬임 방지)
        repo.setCookieName("XSRF-TOKEN");     // 프론트에서 읽는 쿠키 이름
        repo.setHeaderName("X-XSRF-TOKEN");   // 프론트가 보내는 헤더 이름
        return repo;
    }

    // ✅ CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowCredentials(true);

        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "X-XSRF-TOKEN",
                "XSRF-TOKEN",
                "Accept",
                "Origin"
        ));

        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}