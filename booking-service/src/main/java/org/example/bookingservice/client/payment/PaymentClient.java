package org.example.bookingservice.client.payment;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClientBuilder, @Value("${payment.service.url}") String paymentUrl) {
        this.webClient = webClientBuilder.baseUrl(paymentUrl).build();
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return webClient.post()
                .uri("/payments")
                .bodyValue(createPaymentRequest)
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class)
                .block();
    }
}

