package org.example.bookingservice.client.trip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;

@Component
public class TripClient {

    private final WebClient webClient;

    public TripClient(WebClient.Builder webClientBuilder, @Value("${trip.service.url}") String tripUrl) {
        this.webClient = webClientBuilder.baseUrl(tripUrl).build();
    }

    public TripResponse getTrip(UUID tripId) {
        return webClient.get()
                .uri("/trips/{id}", tripId)
                .retrieve()
                .bodyToMono(TripResponse.class)
                .block();
    }
}
