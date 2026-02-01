package org.example.paymentservice.service;

import org.example.paymentservice.dto.PaymentListItemResponse;
import org.example.paymentservice.dto.PaymentStatusResponse;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.model.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID paymentId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "webhookSecret", "whsec_test");
        ReflectionTestUtils.setField(paymentService, "successUrl", "http://test/success");
        ReflectionTestUtils.setField(paymentService, "cancelUrl", "http://test/cancel");
        orderId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderId(orderId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getPaymentByOrderId_whenFound_returnsStatus() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        PaymentStatusResponse result = paymentService.getPaymentByOrderId(orderId);

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(PaymentStatus.PENDING, result.status());
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void getPaymentByOrderId_whenNotFound_throws() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentByOrderId(orderId));
    }

    @Test
    void getPaymentsByOrderIds_emptyList_returnsEmpty() {
        List<PaymentListItemResponse> result = paymentService.getPaymentsByOrderIds(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, never()).findByOrderIdIn(any());
    }

    @Test
    void getPaymentsByOrderIds_null_returnsEmpty() {
        List<PaymentListItemResponse> result = paymentService.getPaymentsByOrderIds(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
