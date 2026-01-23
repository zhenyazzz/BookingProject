package org.example.tripservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(
    name = "routes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_route_from_to",
        columnNames = {"from_city", "to_city"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "from_city", nullable = false, length = 100)
    private String fromCity;

    @Column(name = "to_city", nullable = false, length = 100)
    private String toCity;

}
