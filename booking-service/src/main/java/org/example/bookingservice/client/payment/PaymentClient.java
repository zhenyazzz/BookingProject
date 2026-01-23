package org.example.bookingservice.client.payment;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;

@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClientBuilder, @Value("${payment.service.url}") String paymentUrl) {
        this.webClient = webClientBuilder.baseUrl(paymentUrl).build();
    }

    public String createPayment(UUID orderId) {
        return webClient.post()
                .uri("/api/v1/payments")
                .bodyValue(new CreatePaymentRequest(orderId))
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class)
                .block()
                .paymentUrl();
    }
}

