package org.example.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.example.tripservice.model.Trip;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
    @Query("""
    SELECT t.id
    FROM Trip t
    WHERE t.status = 'SCHEDULED'
    AND t.departureTime <= :now
    """)
    List<UUID> findDepartingTripIds(@Param("now") LocalDateTime now);


    @Modifying
    @Query("""
    UPDATE Trip t
    SET t.status = 'IN_PROGRESS'
    WHERE t.status = 'SCHEDULED'
    AND t.departureTime <= :now
    """)
    int markInProgress(@Param("now") LocalDateTime now);

    @Query("""
      SELECT t.id
      FROM Trip t
      WHERE t.status = 'IN_PROGRESS'
      AND t.arrivalTime <= :now
    """)
    List<UUID> findArrivingTripIds(@Param("now") LocalDateTime now);

    @Modifying
    @Query("""
      UPDATE Trip t
      SET t.status = 'COMPLETED'
      WHERE t.status = 'IN_PROGRESS'
      AND t.arrivalTime <= :now
    """)
    int markCompleted(@Param("now") LocalDateTime now);

}
