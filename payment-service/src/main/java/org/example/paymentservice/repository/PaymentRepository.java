package org.example.paymentservice.repository;

import org.example.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    List<Payment> findByOrderIdIn(List<UUID> orderIds);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        WHERE p.status = 'SUCCEEDED'
        AND (:start IS NULL OR p.createdAt >= :start)
        AND (:end IS NULL OR p.createdAt <= :end)
        """)
    BigDecimal sumSucceededAmountByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT p.status, COUNT(p) FROM Payment p
        WHERE (:start IS NULL OR p.createdAt >= :start)
        AND (:end IS NULL OR p.createdAt <= :end)
        GROUP BY p.status
        """)
    List<Object[]> countByStatusAndCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
