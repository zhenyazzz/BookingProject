package org.example.paymentservice.repository;

import org.example.paymentservice.model.OutboxEvent;
import org.example.paymentservice.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(
        value = """
            SELECT *
            FROM outbox_events
            WHERE status = 'NEW'
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT 100
            """,
        nativeQuery = true
    )
    List<OutboxEvent> findTop100NewForUpdateSkipLocked();

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status, e.sentAt = :sentAt WHERE e.id = :id")
    void markSent(@Param("id") UUID id, @Param("status") OutboxStatus status, @Param("sentAt") LocalDateTime sentAt);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.retryCount = e.retryCount + 1 WHERE e.id = :id")
    void incrementRetry(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status WHERE e.id = :id")
    void markFailed(@Param("id") UUID id, @Param("status") OutboxStatus status);
}
