package org.example.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BookingConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public OpenAPI bookingApi() {
        return new OpenAPI().info(new Info().title("Booking Service API")
                .description("Booking Service API")
                .version("v1.0.0"));
    }
}
