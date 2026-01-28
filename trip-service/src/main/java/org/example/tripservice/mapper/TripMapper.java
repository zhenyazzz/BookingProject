package org.example.tripservice.mapper;

import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.TripResponse;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripCancelledEvent;
import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {RouteMapper.class}, imports = {UUID.class, Instant.class})
public interface TripMapper {

    TripResponse toResponse(Trip trip);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", source = "route")
    @Mapping(target = "totalSeats", source = "request.busType.capacity")
    @Mapping(target = "status", constant = "SCHEDULED")
    Trip toEntity(TripCreateRequest request, Route route);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "busType", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(TripUpdateRequest request, @MappingTarget Trip trip);

    @Mapping(target = "eventId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "tripId", source = "trip.id")
    TripCreatedEvent toCreatedEvent(Trip trip);

    @Mapping(target = "eventId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "cancelledAt", expression = "java(Instant.now())")
    @Mapping(target = "tripId", source = "trip.id")
    TripCancelledEvent toCancelledEvent(Trip trip);
}

