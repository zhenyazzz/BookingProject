package org.example.bookingservice.client.order;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;

@Component
public class OrderClient {

    private final WebClient webClient;

    public OrderClient(WebClient.Builder webClientBuilder, @Value("${order.service.url}") String orderUrl) {
        this.webClient = webClientBuilder.baseUrl(orderUrl).build();
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return webClient.post()
                .uri("/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .block();
    }

    public void cancelOrder(UUID orderId) {
        webClient.put()
                .uri("/orders/{id}/cancel", orderId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void confirmOrder(UUID orderId) {
        webClient.put()
                .uri("/orders/{id}/confirm", orderId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}

