package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.OrderDto;
import org.example.orderservice.kafka.BookingEvent;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent event) {
        log.info("Received event: {}", event);
        Order order = createOrder(event);
        orderRepository.saveAndFlush(order);
        log.info("Order created: {}", order);
    }

    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        log.info("Order found: {}", order);
        return toOrderDto(order);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        inventoryServiceClient.updateTicketCount(
                order.getEventId(),
                order.getTicketCount()
        );
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderDto toOrderDto(Order order) {
        return new OrderDto(order.getId(),order.getTotalPrice(), order.getTicketCount(), order.getEventId());
    }

    private Order createOrder(BookingEvent event) {
        Order order = new Order();
        order.setTotalPrice(event.totalPrice());
        order.setTicketCount(event.ticketCount());
        order.setCustomerId(event.userId());
        order.setEventId(event.eventId());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
}
