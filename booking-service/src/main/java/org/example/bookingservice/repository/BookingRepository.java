package org.example.bookingservice.repository;

import org.example.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    
    /**
     * Находит бронирование по ID заказа
     */
    Optional<Booking> findByOrderId(UUID orderId);
}
