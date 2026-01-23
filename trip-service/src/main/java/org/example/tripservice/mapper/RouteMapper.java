package org.example.tripservice.mapper;

import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.request.RouteUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteResponse toResponse(Route route);

    Route toEntity(RouteCreateRequest request);

    void updateEntity(RouteUpdateRequest request, @MappingTarget Route route);
}

