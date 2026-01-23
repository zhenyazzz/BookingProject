package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.EventInventoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceClient {
    private final RestTemplate restTemplate;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public EventInventoryResponse updateTicketCount(Long eventId, Long ticketCount) {
        String url = inventoryServiceUrl + "/api/v1/event/" + eventId + "/capacity/" + ticketCount;

        EventInventoryResponse response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                EventInventoryResponse.class
        ).getBody();

        log.info("Update ticket count successful: {}", response);
        return response;
    }

}
