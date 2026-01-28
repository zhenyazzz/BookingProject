package org.example.tripservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.util.UUID;

import org.example.kafka.event.BusType;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(
    name = "trips",
    indexes = {
        @Index(name = "idx_trip_status_departure", columnList = "status, departure_time"),
        @Index(name = "idx_trip_status_arrival", columnList = "status, arrival_time"),
        @Index(name = "idx_trip_route", columnList = "route_id"),
        @Index(name = "idx_trip_price", columnList = "price"),
        @Index(name = "idx_trip_departure_only", columnList = "departure_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusType busType;
}

