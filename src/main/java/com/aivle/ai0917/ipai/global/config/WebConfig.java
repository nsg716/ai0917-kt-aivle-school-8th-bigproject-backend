package com.aivle.ai0917.ipai.global.config;

import com.aivle.ai0917.ipai.global.security.jwt.CurrentUserIdArgumentResolver;
import com.aivle.ai0917.ipai.global.security.interceptor.LastActivityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LastActivityInterceptor lastActivityInterceptor;
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastActivityInterceptor)
                .addPathPatterns("/api/**"); // 최소 이 정도는 걸어야 의미 있음
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
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
