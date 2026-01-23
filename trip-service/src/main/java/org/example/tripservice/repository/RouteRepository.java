package org.example.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.example.tripservice.model.Route;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {
    
    Page<Route> findByFromCityContainingIgnoreCaseAndToCityContainingIgnoreCase(
        String fromCity,
        String toCity,
        Pageable pageable
    );

    boolean existsByFromCityAndToCity(String fromCity, String toCity);
}
