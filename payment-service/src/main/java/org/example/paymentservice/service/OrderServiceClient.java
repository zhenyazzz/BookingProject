package org.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.dto.OrderDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceClient {
    @Value("${order.service.url}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate;

    public OrderDto getOrder(Long orderId) {
        String url = orderServiceUrl + "/api/v1/orders/" + orderId;
        try {
            ResponseEntity<OrderDto> response = restTemplate.getForEntity(url, OrderDto.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get order: {}", orderId, e);
            throw new RuntimeException("Order service unavailable");
        }
    }

    public void confirmOrder(Long orderId) {
        String url = orderServiceUrl + "/api/v1/orders/" + orderId + "/confirm";
        try {
            restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
            log.info("Order confirmed: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to confirm order: {}", orderId, e);
            throw new RuntimeException("Failed to confirm order");
        }
    }

    public void cancelOrder(Long orderId) {
        String url = orderServiceUrl + "/api/v1/orders/" + orderId + "/cancel";
        try {
            restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
            log.info("Order cancelled: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to cancel order: {}", orderId, e);
            throw new RuntimeException("Failed to cancel order");
        }
    }
}
