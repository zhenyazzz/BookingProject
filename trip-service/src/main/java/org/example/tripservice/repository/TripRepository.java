package org.example.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.example.tripservice.model.Trip;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
}
