package org.example.tripservice.repository;

import org.example.kafka.event.BusType;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.example.tripservice.model.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class TripRepositoryTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route;
    private Trip scheduledTrip;
    private Trip inProgressTrip;
    private Trip futureTrip;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeRepository.deleteAll();

        route = new Route();
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");
        route = routeRepository.save(route);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(2);
        LocalDateTime future = now.plusHours(2);

        scheduledTrip = createTrip(route, past, now.plusHours(4), TripStatus.SCHEDULED);
        inProgressTrip = createTrip(route, past, now.plusHours(1), TripStatus.IN_PROGRESS);
        futureTrip = createTrip(route, future, future.plusHours(4), TripStatus.SCHEDULED);
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
    void findDepartingTripIds_ShouldReturnScheduledTripsWithPastDepartureTime() {
        LocalDateTime now = LocalDateTime.now();

        List<UUID> departingIds = tripRepository.findDepartingTripIds(now);

        assertEquals(1, departingIds.size());
        assertTrue(departingIds.contains(scheduledTrip.getId()));
        assertFalse(departingIds.contains(futureTrip.getId()));
        assertFalse(departingIds.contains(inProgressTrip.getId()));
    }

    @Test
    void markInProgress_ShouldUpdateScheduledTripsWithPastDepartureTime() {
        LocalDateTime now = LocalDateTime.now();

        int updated = tripRepository.markInProgress(now);

        assertEquals(1, updated);
        
        Trip updatedTrip = tripRepository.findById(scheduledTrip.getId()).orElseThrow();
        assertEquals(TripStatus.IN_PROGRESS, updatedTrip.getStatus());
        
        Trip futureTripAfterUpdate = tripRepository.findById(futureTrip.getId()).orElseThrow();
        assertEquals(TripStatus.SCHEDULED, futureTripAfterUpdate.getStatus());
    }

    @Test
    void findArrivingTripIds_ShouldReturnInProgressTripsWithPastArrivalTime() {
        LocalDateTime now = LocalDateTime.now();

        List<UUID> arrivingIds = tripRepository.findArrivingTripIds(now);

        assertEquals(1, arrivingIds.size());
        assertTrue(arrivingIds.contains(inProgressTrip.getId()));
        assertFalse(arrivingIds.contains(scheduledTrip.getId()));
    }

    @Test
    void markCompleted_ShouldUpdateInProgressTripsWithPastArrivalTime() {
        LocalDateTime now = LocalDateTime.now();

        int updated = tripRepository.markCompleted(now);

        assertEquals(1, updated);
        
        Trip updatedTrip = tripRepository.findById(inProgressTrip.getId()).orElseThrow();
        assertEquals(TripStatus.COMPLETED, updatedTrip.getStatus());
    }

    @Test
    void saveAndFindById_Success() {
        Trip newTrip = createTrip(route, 
                LocalDateTime.now().plusDays(1), 
                LocalDateTime.now().plusDays(1).plusHours(4), 
                TripStatus.SCHEDULED);

        assertNotNull(newTrip.getId());
        
        Trip found = tripRepository.findById(newTrip.getId()).orElseThrow();
        assertEquals(newTrip.getId(), found.getId());
        assertEquals(TripStatus.SCHEDULED, found.getStatus());
    }
}
