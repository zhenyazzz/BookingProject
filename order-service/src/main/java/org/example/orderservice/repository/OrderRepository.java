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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    
    /**
     * Рассчитывает общий доход за период
     */
    @Query("""
        SELECT COALESCE(SUM(o.totalPrice), 0)
        FROM Order o
        WHERE o.status = 'CONFIRMED'
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
        """)
    BigDecimal calculateTotalRevenue(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
    /**
     * Получает статистику доходов по дням
     */
    @Query("""
        SELECT DATE(o.createdAt) as date, 
               SUM(o.totalPrice) as revenue, 
               COUNT(o) as orderCount
        FROM Order o
        WHERE o.status = 'CONFIRMED'
        AND (:startDate IS NULL OR DATE(o.createdAt) >= :startDate)
        AND (:endDate IS NULL OR DATE(o.createdAt) <= :endDate)
        GROUP BY DATE(o.createdAt)
        ORDER BY date DESC
        """)
    List<Object[]> getDailyRevenueStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Рассчитывает средний чек
     */
    @Query("""
        SELECT COALESCE(AVG(o.totalPrice), 0)
        FROM Order o
        WHERE o.status = 'CONFIRMED'
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
        """)
    BigDecimal calculateAverageOrderValue(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
    /**
     * Подсчитывает количество заказов по статусам
     */
    @Query("""
        SELECT o.status, COUNT(o)
        FROM Order o
        WHERE (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
        GROUP BY o.status
        """)
    List<Object[]> countOrdersByStatus(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
