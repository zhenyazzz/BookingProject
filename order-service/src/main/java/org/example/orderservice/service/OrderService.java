package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.OrderCancelledEvent;
import org.example.kafka.event.OrderConfirmedEvent;
import org.example.kafka.event.OrderCreatedEvent;
import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.kafka.event.ReservationExpiredEvent;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.client.InventoryServiceClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.example.orderservice.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order: userId={}, tripId={}, seatsCount={}, reservationId={}, price={}", 
                request.userId(), request.tripId(), request.seatsCount(), request.reservationId(), request.price());
        
        BigDecimal totalPrice = request.price().multiply(BigDecimal.valueOf(request.seatsCount()));
        
        Order order = Order.builder()
                .userId(request.userId())
                .tripId(request.tripId())
                .seatsCount(request.seatsCount())
                .totalPrice(totalPrice)
                .reservationId(request.reservationId())
                .status(OrderStatus.PENDING)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderId={}", savedOrder.getId());
        
        publishOrderCreatedEvent(savedOrder);
        
        return orderMapper.toResponse(savedOrder);
    }

    

    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        log.debug("Order found: orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> getAllOrders(UUID userId, UUID tripId, OrderStatus status, Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (tripId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tripId"), tripId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toResponse);
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        log.info("Confirming order: orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order is not in PENDING status: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        order.confirm();
        orderRepository.save(order);

        if (order.getReservationId() != null) {
            inventoryServiceClient.confirmReservation(order.getReservationId());
            log.debug("Reservation confirmed: reservationId={}", order.getReservationId());
        }

        publishOrderConfirmedEvent(order);
        
        log.info("Order confirmed: orderId={}", orderId);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        log.info("Cancelling order: orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.debug("Order already cancelled: orderId={}", orderId);
            return;
        }

        cancelOrderProcess(order);
        log.info("Order cancelled: orderId={}", orderId);
    }

    @Transactional
    public void handleReservationExpired(ReservationExpiredEvent event) {
        log.info("Handling reservation expired event: reservationId={}", event.reservationId());
        
        orderRepository.findByReservationId(event.reservationId())
                .ifPresentOrElse(
                        order -> {
                            if (order.getStatus() == OrderStatus.PENDING) {
                                order.cancel();
                                orderRepository.save(order);
                                publishOrderCancelledEvent(order);
                            } else {
                                log.debug("Order is not in PENDING status, skipping: orderId={}, status={}", 
                                        order.getId(), order.getStatus());
                            }
                        },
                        () -> log.warn("Order not found for reservationId: {}", event.reservationId())
                );
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Handling payment succeeded event: orderId={}", event.orderId());
        confirmOrder(event.orderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Handling payment failed event: orderId={}, reason={}", event.orderId(), event.reason());
        cancelOrder(event.orderId());
    }

    private void cancelOrderProcess(Order order) {
        log.info("Processing order cancellation: orderId={}", order.getId());
        
        order.cancel();
        orderRepository.save(order);

        if (order.getReservationId() != null) {
            try {
                inventoryServiceClient.releaseReservation(order.getReservationId());
                log.debug("Reservation released: reservationId={}", order.getReservationId());
            } catch (Exception ex) {
                log.error("Failed to release reservation: {}", order.getReservationId(), ex);
            }
        }

        publishOrderCancelledEvent(order);
    }

    private void publishOrderCreatedEvent(Order order) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    UUID.randomUUID(),
                    order.getId(),
                    Instant.now()
            );
            kafkaTemplate.send("order.created", order.getId().toString(), event);
            log.info("Order created event published: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish order created event: orderId={}", order.getId(), e);
        }
    }

    private void publishOrderConfirmedEvent(Order order) {
        try {
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    UUID.randomUUID(),
                    order.getId(),
                    Instant.now()
            );
            kafkaTemplate.send("order.confirmed", order.getId().toString(), event);
            log.info("Order confirmed event published: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish order confirmed event: orderId={}", order.getId(), e);
        }
    }

    private void publishOrderCancelledEvent(Order order) {
        try {
            OrderCancelledEvent event = new OrderCancelledEvent(
                    UUID.randomUUID(),
                    order.getId(),
                    Instant.now()
            );
            kafkaTemplate.send("order.cancelled", order.getId().toString(), event);
            log.info("Order cancelled event published: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish order cancelled event: orderId={}", order.getId(), e);
        }
    }
}
