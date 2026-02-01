package org.example.bookingservice.repository;

import org.example.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    

    Optional<Booking> findByOrderId(UUID orderId);

    @Query("""
        SELECT b.status, COUNT(b)
        FROM Booking b
        WHERE (:startDate IS NULL OR b.createdAt >= :startDate)
        AND (:endDate IS NULL OR b.createdAt <= :endDate)
        GROUP BY b.status
        """)
    List<Object[]> countBookingsByStatus(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
    
    @Query("""
        SELECT DATE(b.createdAt) as date, 
               COUNT(b) as bookingCount,
               SUM(b.seatsCount) as totalSeats
        FROM Booking b
        WHERE (:startDate IS NULL OR DATE(b.createdAt) >= :startDate)
        AND (:endDate IS NULL OR DATE(b.createdAt) <= :endDate)
        GROUP BY DATE(b.createdAt)
        ORDER BY date DESC
        """)
    List<Object[]> getDailyBookingStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    
    @Query("""
        SELECT 
            COUNT(CASE WHEN b.status = 'CONFIRMED' THEN 1 END) as confirmed,
            COUNT(b) as total
        FROM Booking b
        WHERE (:startDate IS NULL OR b.createdAt >= :startDate)
        AND (:endDate IS NULL OR b.createdAt <= :endDate)
        """)
    Object[] calculateConversionRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
  
    @Query("""
        SELECT b.tripId, COUNT(b) as bookingCount, SUM(b.seatsCount) as totalSeats
        FROM Booking b
        WHERE b.status = 'CONFIRMED'
        AND (:startDate IS NULL OR b.createdAt >= :startDate)
        AND (:endDate IS NULL OR b.createdAt <= :endDate)
        GROUP BY b.tripId
        ORDER BY bookingCount DESC
        """)
    List<Object[]> getPopularTrips(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
