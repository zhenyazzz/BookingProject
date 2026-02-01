package org.example.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.example.tripservice.model.Route;
import java.time.LocalDate;
import java.util.List;
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
    

    @Query("""
        SELECT r.id, r.fromCity, r.toCity, COUNT(t.id) as tripCount
        FROM Route r
        LEFT JOIN Trip t ON t.route.id = r.id
        WHERE (:startDate IS NULL OR DATE(t.departureTime) >= :startDate)
        AND (:endDate IS NULL OR DATE(t.departureTime) <= :endDate)
        GROUP BY r.id, r.fromCity, r.toCity
        ORDER BY tripCount DESC
        """)
    List<Object[]> getRoutesWithTripCount(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT DISTINCT r.fromCity
        FROM Route r
        ORDER BY r.fromCity
        """)
    List<String> findAllDistinctFromCities();

    @Query("""
        SELECT DISTINCT r.toCity
        FROM Route r
        ORDER BY r.toCity
        """)
    List<String> findAllDistinctToCities();

    @Query(value = """
        SELECT DISTINCT city FROM (
            SELECT r.from_city as city 
            FROM routes r 
            JOIN trips t ON r.id = t.route_id 
            WHERE t.status = 'SCHEDULED'
            UNION
            SELECT r.to_city as city 
            FROM routes r 
            JOIN trips t ON r.id = t.route_id 
            WHERE t.status = 'SCHEDULED'
        ) AS distinct_cities
        ORDER BY city
        """, nativeQuery = true)
    List<String> findAllDistinctCitiesWithScheduledTrips();
}
