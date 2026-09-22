package com.priceintel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI priceIntelOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Price Intel API")
                        .description("Product discovery, recommendation scoring, price tracking, and market alerts for cross-border e-commerce.")
                        .version("1.0.0"));
    }
}
