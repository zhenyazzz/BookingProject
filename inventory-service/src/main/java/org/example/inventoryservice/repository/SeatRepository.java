package org.example.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.model.SeatStatus;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    boolean existsByTripId(UUID tripId);

    List<Seat> findByTripId(UUID tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Seat> findByTripIdAndSeatNumberIn(UUID tripId, List<Integer> seatNumbers);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status, s.lastStatusUpdate = CURRENT_TIMESTAMP WHERE s.tripId = :tripId")
    void updateStatusByTripId(@Param("tripId") UUID tripId, @Param("status") SeatStatus status);

    @Query("SELECT s FROM Seat s WHERE s.lastStatusUpdate < :threshold AND s.status = 'RESERVED'")
    List<Seat> findExpiredReservations(@Param("threshold") LocalDateTime threshold);
}
