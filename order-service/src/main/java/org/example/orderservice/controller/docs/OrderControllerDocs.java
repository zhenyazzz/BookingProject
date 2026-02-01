package org.example.orderservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Orders", description = "API for managing booking orders")
public interface OrderControllerDocs {

    @Operation(summary = "Create an order", description = "Creates a new order based on the provided request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Trip or User not found")
    })
    ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request);

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId);

    @Operation(summary = "Get my orders", description = "Retrieves a paginated list of orders for the current authenticated user.")
    @ApiResponse(responseCode = "200", description = "List of current user's orders")
    ResponseEntity<Page<OrderResponse>> getMyOrders(Pageable pageable);

    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of orders with optional filtering by user, trip, or status. Non-admin users only see their own orders.")
    @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID tripId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
    );

    @Operation(summary = "Confirm order", description = "Marks an order as confirmed (PAID).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order confirmed successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<Void> confirmOrder(@PathVariable UUID orderId);

    @Operation(summary = "Cancel order", description = "Cancels an order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId);
}
