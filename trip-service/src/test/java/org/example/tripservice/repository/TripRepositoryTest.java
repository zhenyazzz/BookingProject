package org.example.tripservice.repository;

import org.example.kafka.event.BusType;
import org.example.tripservice.config.SecurityConfig;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.example.tripservice.model.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Tag("repository")
@DataJpaTest
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@ActiveProfiles("test")
@Transactional
class TripRepositoryTest extends BasePostgresRepositoryTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private EntityManager entityManager;

    private Route route;
    private Trip scheduledTrip;
    private Trip futureTrip;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeRepository.deleteAll();

        route = new Route();
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");
        route = routeRepository.save(route);

        baseTime = LocalDateTime.now();
        LocalDateTime pastDeparture = baseTime.minusHours(3);
        LocalDateTime futureDeparture = baseTime.plusHours(2);
        LocalDateTime futureArrival = baseTime.plusHours(6);

        scheduledTrip = createTrip(route, pastDeparture, baseTime.plusHours(4), TripStatus.SCHEDULED);
        futureTrip = createTrip(route, futureDeparture, futureArrival, TripStatus.SCHEDULED);
    }

    private Trip createTrip(Route route, LocalDateTime departure, LocalDateTime arrival, TripStatus status) {
        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setDepartureTime(departure);
        trip.setArrivalTime(arrival);
        trip.setPrice(BigDecimal.valueOf(1500));
        trip.setTotalSeats(50);
        trip.setStatus(status);
        trip.setBusType(BusType.BUS_50);
        return tripRepository.save(trip);
    }

    @Test
    void markInProgressAndReturnIds_ShouldUpdateScheduledTrips() {
        LocalDateTime now = baseTime;

        var updatedIds = tripRepository.markInProgressAndReturnIds(now);

        assertEquals(1, updatedIds.size());
        assertTrue(updatedIds.contains(scheduledTrip.getId()));
        
        entityManager.flush();
        entityManager.clear();
        
        Trip updatedTrip = tripRepository.findById(scheduledTrip.getId()).orElseThrow();
        assertEquals(TripStatus.IN_PROGRESS, updatedTrip.getStatus());
        
        Trip futureTripAfterUpdate = tripRepository.findById(futureTrip.getId()).orElseThrow();
        assertEquals(TripStatus.SCHEDULED, futureTripAfterUpdate.getStatus());
    }

    @Test
    void markCompletedAndReturnIds_NoMatches() {
        LocalDateTime now = baseTime;

        var arrivingIds = tripRepository.markCompletedAndReturnIds(now);

        assertTrue(arrivingIds.isEmpty());
    }
}
