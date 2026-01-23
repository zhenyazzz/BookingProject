package org.example.tripservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Trip Service API")
                        .description("Trip Service API")
                        .version("v1.0.0"));
    }
}