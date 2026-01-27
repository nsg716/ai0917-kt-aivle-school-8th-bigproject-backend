package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.interceptor.LastActivityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LastActivityInterceptor lastActivityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastActivityInterceptor)
                .addPathPatterns("/api/**"); // 최소 이 정도는 걸어야 의미 있음
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600)
         //쿠키/세션을 쓰는 경우만 allowCredentials(true) 추가
                .allowCredentials(true);
    }
}
