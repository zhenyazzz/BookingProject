package org.example.tripservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.TripResponse;
import org.example.tripservice.exception.RouteNotFoundException;
import org.example.tripservice.exception.TripNotFoundException;
import org.example.tripservice.mapper.TripMapper;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.example.tripservice.repository.RouteRepository;
import org.example.tripservice.repository.TripRepository;
import org.example.tripservice.repository.TripSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import io.micrometer.observation.annotation.Observed;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "trips")
@Observed(name = "trip.service")
public class TripService {
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final TripMapper tripMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "trip-pages", allEntries = true)
    public TripResponse createTrip(TripCreateRequest request) {
        if (request.arrivalTime().isBefore(request.departureTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before departure time");
        }

        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new RouteNotFoundException("Route not found with id: " + request.routeId()));
        
        Trip trip = tripMapper.toEntity(request, route);
        
        Trip savedTrip = tripRepository.save(trip);
        
        log.info("Trip created: ID={}, Route={} -> {}, Time={}", 
                savedTrip.getId(), route.getFromCity(), route.getToCity(), savedTrip.getDepartureTime());
        
        TripCreatedEvent event = tripMapper.toCreatedEvent(savedTrip);

        kafkaTemplate.send("trip.created", event.tripId().toString(), event);

        return tripMapper.toResponse(savedTrip);
    }

    @Cacheable(key = "#id")
    public TripResponse getTripById(UUID id) {
        log.info("Fetching trip by id: {}", id);
        return tripRepository.findById(id)
                .map(tripMapper::toResponse)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));
    }

    public Page<TripResponse> getTrips(String fromCity, String toCity, LocalDate date, Pageable pageable) {
        log.info("Searching trips: from={}, to={}, date={}", fromCity, toCity, date);
        
        Specification<Trip> spec = Specification.where(TripSpecifications.withFromCity(fromCity))
                .and(TripSpecifications.withToCity(toCity))
                .and(TripSpecifications.withDepartureDate(date))
                .and(TripSpecifications.withDepartureNotInPast());
                
        return tripRepository.findAll(spec, pageable)
                .map(tripMapper::toResponse);
    }

    @Transactional
    @CachePut(key = "#id")
    @CacheEvict(value = "trip-pages", allEntries = true)
    public TripResponse updateTrip(UUID id, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));
        
        if (request.departureTime() != null && request.arrivalTime() != null) {
            if (request.arrivalTime().isBefore(request.departureTime())) {
                throw new IllegalArgumentException("Arrival time cannot be before departure time");
            }
        } else if (request.departureTime() != null) {
            if (trip.getArrivalTime().isBefore(request.departureTime())) {
                throw new IllegalArgumentException("New departure time cannot be after existing arrival time");
            }
        } else if (request.arrivalTime() != null) {
            if (request.arrivalTime().isBefore(trip.getDepartureTime())) {
                throw new IllegalArgumentException("New arrival time cannot be before existing departure time");
            }
        }

        if (request.routeId() != null) {
            Route route = routeRepository.findById(request.routeId())
                    .orElseThrow(() -> new RouteNotFoundException("Route not found with id: " + request.routeId()));
            trip.setRoute(route);
        }

        tripMapper.updateEntity(request, trip);
        Trip updatedTrip = tripRepository.save(trip);
        
        log.info("Trip updated: ID={}, New Time={}, New Route={}", 
                updatedTrip.getId(), updatedTrip.getDepartureTime(), 
                updatedTrip.getRoute() != null ? updatedTrip.getRoute().getId() : "N/A");
        
        return tripMapper.toResponse(updatedTrip);
    }

    @Transactional
    @CacheEvict(value = "trip-pages", allEntries = true)
    public void deleteTripById(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));
        
        tripRepository.delete(trip);
        log.info("Trip deleted: ID={}", id);
        
        TripCancelledEvent event = tripMapper.toCancelledEvent(trip);
        kafkaTemplate.send("trip.cancelled", event.tripId().toString(), event);
    }

}
