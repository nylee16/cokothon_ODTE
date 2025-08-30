package com.odte.topicurator.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI topicuratorOpenAPI() {
        // 보안 스키마 정의 (JWT Bearer)
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 전역 보안 요구사항 추가 -> 모든 Operation에 자물쇠 & Authorize 버튼 노출
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info().title("Topicurator API").version("v1"))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(securityRequirement);
    }
}
