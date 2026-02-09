package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.controller.docs.OrderControllerDocs;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {
    private final OrderService orderService;

    @Override
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID currentUserId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(orderService.createOrder(request, currentUserId));
    }

    @Override
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID currentUserId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(orderService.getAllOrders(currentUserId, null, null, pageable));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID tripId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID effectiveUserId = userId;
        if (!SecurityUtils.hasRole("ADMIN")) {
            effectiveUserId = SecurityUtils.currentUserId();
        }
        return ResponseEntity.ok(orderService.getAllOrders(effectiveUserId, tripId, status, pageable));
    }

    @Override
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable UUID orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
