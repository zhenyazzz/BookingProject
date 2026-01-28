package org.example.tripservice.repository;

import org.example.tripservice.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RouteRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;
    private Route route2;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        route1 = new Route();
        route1.setFromCity("Moscow");
        route1.setToCity("Saint Petersburg");
        route1 = routeRepository.save(route1);

        route2 = new Route();
        route2.setFromCity("Berlin");
        route2.setToCity("Munich");
        route2 = routeRepository.save(route2);
    }

    @Test
    void save_Success() {
        Route newRoute = new Route();
        newRoute.setFromCity("New York");
        newRoute.setToCity("Los Angeles");

        Route saved = routeRepository.save(newRoute);

        assertNotNull(saved.getId());
        assertEquals("New York", saved.getFromCity());
        assertEquals("Los Angeles", saved.getToCity());
    }

    @Test
    void findById_Success() {
        Optional<Route> found = routeRepository.findById(route1.getId());

        assertTrue(found.isPresent());
        assertEquals(route1.getId(), found.get().getId());
        assertEquals("Moscow", found.get().getFromCity());
        assertEquals("Saint Petersburg", found.get().getToCity());
    }

    @Test
    void findByFromCityContainingIgnoreCaseAndToCityContainingIgnoreCase_ExactMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Route> result = routeRepository.findByFromCityContainingIgnoreCaseAndToCityContainingIgnoreCase(
                "Moscow", "Saint Petersburg", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Moscow", result.getContent().get(0).getFromCity());
        assertEquals("Saint Petersburg", result.getContent().get(0).getToCity());
    }

    @Test
    void existsByFromCityAndToCity_Exists() {
        boolean exists = routeRepository.existsByFromCityAndToCity("Moscow", "Saint Petersburg");

        assertTrue(exists);
    }

    @Test
    void delete_Success() {
        routeRepository.delete(route1);

        Optional<Route> deleted = routeRepository.findById(route1.getId());
        assertFalse(deleted.isPresent());
    }
}
