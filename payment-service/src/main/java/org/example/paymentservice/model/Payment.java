package org.example.paymentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_order_id", columnNames = "order_id"),
        @UniqueConstraint(name = "uk_payments_payment_intent_id", columnNames = "payment_intent_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;
    @Column(name = "payment_intent_id", unique = true)
    private String paymentIntentId;
    @Column(name = "checkout_session_id")
    private String checkoutSessionId;
    @Column(name = "checkout_session_url")
    private String checkoutSessionUrl;
    private String clientSecret;

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

}
