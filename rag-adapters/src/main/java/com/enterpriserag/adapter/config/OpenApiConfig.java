package com.enterpriserag.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise RAG Assistant")
                        .version("v1")
                        .description("""
                                Production-grade RAG platform.
                                Upload documents → async ingestion via Kafka → ask questions → grounded answers with citations.
                                """));
    }
}
