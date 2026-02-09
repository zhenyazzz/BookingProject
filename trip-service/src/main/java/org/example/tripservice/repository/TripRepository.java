package org.example.tripservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.example.tripservice.model.Trip;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {

    @Override
    @EntityGraph(attributePaths = {"route"})
    Page<Trip> findAll(Specification<Trip> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"route"})
    Optional<Trip> findById(UUID id);

    @Query(
        value = """
            UPDATE trips
            SET status = 'IN_PROGRESS'
            WHERE status = 'SCHEDULED'
              AND departure_time <= :now
            RETURNING id
            """,
        nativeQuery = true
    )
    List<UUID> markInProgressAndReturnIds(@Param("now") LocalDateTime now);

    @Query(
        value = """
            UPDATE trips
            SET status = 'COMPLETED'
            WHERE status = 'IN_PROGRESS'
              AND arrival_time <= :now
            RETURNING id
            """,
        nativeQuery = true
    )
    List<UUID> markCompletedAndReturnIds(@Param("now") LocalDateTime now);

}
