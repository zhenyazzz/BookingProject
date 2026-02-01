package org.example.inventoryservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class TripServiceClient {
    private final RestTemplate restTemplate;
    private final String tripServiceUrl;

    public TripServiceClient(
            RestTemplate restTemplate,
            @Value("${trip.service.url}") String tripServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.tripServiceUrl = tripServiceUrl;
    }

    public TripResponse getTripById(UUID tripId) {
        try {
            String url = tripServiceUrl + "/trips/" + tripId;
            return restTemplate.getForObject(url, TripResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch trip info for tripId: " + tripId, e);
        }
    }
}
