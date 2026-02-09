package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.EventType;
import org.example.kafka.event.OrderCancelledEvent;
import org.example.kafka.event.OrderConfirmedEvent;
import org.example.kafka.event.OrderCreatedEvent;
import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.kafka.event.ReservationExpiredEvent;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.client.InventoryServiceClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.util.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final OutboxService outboxService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        log.info("Creating order: userId={}, tripId={}, seatsCount={}, reservationId={}, price={}",
                userId, request.tripId(), request.seatsCount(), request.reservationId(), request.price());

        BigDecimal totalPrice = request.price().multiply(BigDecimal.valueOf(request.seatsCount()));

        Order order = Order.builder()
                .userId(userId)
                .tripId(request.tripId())
                .seatsCount(request.seatsCount())
                .totalPrice(totalPrice)
                .reservationId(request.reservationId())
                .status(OrderStatus.PENDING)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderId={}", savedOrder.getId());

        outboxService.saveEvent(
                savedOrder.getId(),
                EventType.ORDER_CREATED,
                new OrderCreatedEvent(UUID.randomUUID(), savedOrder.getId(), Instant.now())
        );

        return orderMapper.toResponse(savedOrder);
    }

    

    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        UUID currentUserId = SecurityUtils.currentUserId();
        if (!order.getUserId().equals(currentUserId) && !SecurityUtils.hasRole("ADMIN")) {
            throw new AccessDeniedException("You are not authorized to view this order");
        }
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        UUID currentUserId = SecurityUtils.currentUserId();
        if (!order.getUserId().equals(currentUserId) && !SecurityUtils.hasRole("ADMIN")) {
            throw new AccessDeniedException("You are not authorized to confirm this order");
        }
        doConfirmOrder(order);
    }

    @Transactional
    public void applyPaymentSucceeded(UUID orderId) {
        log.info("Applying payment succeeded: orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        doConfirmOrder(order);
    }

    private void doConfirmOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order is not in PENDING status: orderId={}, status={}", order.getId(), order.getStatus());
            return;
        }
        order.confirm();
        orderRepository.save(order);
        if (order.getReservationId() != null) {
            inventoryServiceClient.confirmReservation(order.getReservationId());
            log.debug("Reservation confirmed: reservationId={}", order.getReservationId());
        }
        outboxService.saveEvent(
                order.getId(),
                EventType.ORDER_CONFIRMED,
                new OrderConfirmedEvent(UUID.randomUUID(), order.getId(), Instant.now())
        );
        log.info("Order confirmed: orderId={}", order.getId());
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        UUID currentUserId = SecurityUtils.currentUserId();
        if (!order.getUserId().equals(currentUserId) && !SecurityUtils.hasRole("ADMIN")) {
            throw new AccessDeniedException("You are not authorized to cancel this order");
        }
        doCancelOrder(order);
    }

    @Transactional
    public void applyOrderCancelled(UUID orderId) {
        log.info("Applying order cancellation (event): orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Skipping cancellation for non-PENDING order: orderId={}, status={}", order.getId(), order.getStatus());
            return;
        }
        doCancelOrder(order);
    }

    private void doCancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.debug("Order already cancelled: orderId={}", order.getId());
            return;
        }
        cancelOrderProcess(order);
        log.info("Order cancelled: orderId={}", order.getId());
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
                                outboxService.saveEvent(
                                        order.getId(),
                                        EventType.ORDER_CANCELLED,
                                        new OrderCancelledEvent(UUID.randomUUID(), order.getId(), Instant.now())
                                );
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
        applyPaymentSucceeded(event.orderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Handling payment failed event: orderId={}, reason={}", event.orderId(), event.reason());
        applyOrderCancelled(event.orderId());
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

        outboxService.saveEvent(
                order.getId(),
                EventType.ORDER_CANCELLED,
                new OrderCancelledEvent(UUID.randomUUID(), order.getId(), Instant.now())
        );
    }
}
