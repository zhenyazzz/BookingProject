package org.example.tripservice.repository;

import org.springframework.data.jpa.domain.Specification;
import org.example.tripservice.model.Trip;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TripSpecifications {

    private TripSpecifications() {}

    public static Specification<Trip> withFromCity(String fromCity) {
        return (root, query, cb) ->
            (fromCity == null || fromCity.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("route").get("fromCity"), fromCity);
    }

    public static Specification<Trip> withToCity(String toCity) {
        return (root, query, cb) ->
            (toCity == null || toCity.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("route").get("toCity"), toCity);
    }

    public static Specification<Trip> withDepartureDate(LocalDate date) {
        if (date == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return (root, query, cb) ->
            cb.between(root.get("departureTime"), start, end);
    }

    public static Specification<Trip> withDepartureNotInPast() {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("departureTime"), LocalDateTime.now());
    }
}
