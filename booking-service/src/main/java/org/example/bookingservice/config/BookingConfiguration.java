package org.example.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.example.bookingservice.util.BearerTokenHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class BookingConfiguration {

    private static ExchangeFilterFunction forwardBearerToken() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String token = BearerTokenHolder.getToken();
            if (token != null && !token.isBlank()) {
                ClientRequest mutated = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .build();
                return Mono.just(mutated);
            }
            return Mono.just(request);
        });
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(forwardBearerToken());
    }

    @Bean
    @Qualifier("tripServiceWebClient")
    public WebClient tripServiceWebClient(@Value("${trip.service.url}") String tripUrl) {
        return WebClient.builder()
                .baseUrl(tripUrl)
                .build();
    }

    @Bean
    public OpenAPI bookingApi() {
        return new OpenAPI().info(new Info().title("Booking Service API")
                .description("Booking Service API")
                .version("v1.0.0"));
    }
}
