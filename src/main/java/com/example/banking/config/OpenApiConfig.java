package com.example.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Banking API")
                        .description("API for banking application")
                        .version("v1.0"));
    }
}