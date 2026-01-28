package org.example.orderservice.repository;

import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    
    /**
     * Находит заказ по reservationId
     */
    Optional<Order> findByReservationId(UUID reservationId);
    
    /**
     * Находит все заказы пользователя
     */
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Находит все заказы по рейсу
     */
    Page<Order> findByTripId(UUID tripId, Pageable pageable);
    
    /**
     * Находит все заказы по статусу
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
