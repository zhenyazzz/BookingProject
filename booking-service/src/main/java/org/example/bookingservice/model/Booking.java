package org.example.bookingservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.time.Instant;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID tripId;

    @Column(nullable = false)
    private int seatsCount;

    @Column
    private UUID reservationId;

    @Column
    private Instant reservationExpiresAt;

    @Column
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void reserveSeats(UUID reservationId, Instant expiresAt) {
        this.reservationId = reservationId;
        this.reservationExpiresAt = expiresAt;
        this.status = BookingStatus.SEATS_RESERVED;
    }

    public void waitForPayment(UUID orderId) {
        this.orderId = orderId;
        this.status = BookingStatus.WAITING_PAYMENT;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }
}

