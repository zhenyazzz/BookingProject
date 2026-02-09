package org.example.orderservice.service;

import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.client.InventoryServiceClient;
import org.example.orderservice.mapper.OrderMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private InventoryServiceClient inventoryServiceClient;
    @Mock
    private OutboxService outboxService;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UUID orderId;
    private UUID userId;
    private UUID tripId;
    private UUID reservationId;
    private CreateOrderRequest request;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        request = new CreateOrderRequest(tripId, reservationId, BigDecimal.valueOf(50), 2);
        order = Order.builder()
                .id(orderId)
                .userId(userId)
                .tripId(tripId)
                .reservationId(reservationId)
                .seatsCount(2)
                .totalPrice(BigDecimal.valueOf(100))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        orderResponse = new OrderResponse(orderId, BigDecimal.valueOf(100), 2, tripId, userId, OrderStatus.PENDING, Instant.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setCurrentUser(UUID subjectUserId) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subjectUserId.toString());
        Authentication auth = new AbstractAuthenticationToken(Collections.emptyList()) {
            @Override
            public Object getPrincipal() { return jwt; }
            @Override
            public Object getCredentials() { return null; }
        };
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createOrder_returnsResponse() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(orderId);
            return o;
        });
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request, userId);

        assertNotNull(result);
        assertEquals(orderId, result.id());
        assertEquals(OrderStatus.PENDING, result.status());
        verify(orderRepository).save(any(Order.class));
        verify(outboxService).saveEvent(eq(orderId), any(), any());
    }

    @Test
    void getOrderById_whenFound_returnsResponse() {
        setCurrentUser(userId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.id());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_whenNotFound_throws() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(orderId));
    }
}
