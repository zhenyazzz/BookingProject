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

@Mapper(componentModel = "spring", uses = {RouteMapper.class})
public interface TripMapper {

    TripResponse toResponse(Trip trip);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", source = "route")
    Trip toEntity(TripCreateRequest request, Route route);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    void updateEntity(TripUpdateRequest request, @MappingTarget Trip trip);
}

