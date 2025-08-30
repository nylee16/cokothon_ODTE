package com.odte.topicurator.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",  // React, Vue 개발 서버 기본 포트
                        "http://localhost:8080",  // 다른 로컬 서버 포트
                        "http://localhost:5173"   // Vite 개발 서버 기본 포트
                        // 운영 환경 프론트엔드 도메인을 아래에 추가하세요.
                        // 예: "https://your-frontend-domain.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*")        // 모든 헤더 허용
                .allowCredentials(true)     // 쿠키, 인증정보 포함 허용
                .maxAge(3600);              // Pre-flight 요청 캐싱 시간 (초)
    }
}
