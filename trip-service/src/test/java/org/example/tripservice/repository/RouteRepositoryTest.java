package org.example.tripservice.repository;

import org.example.tripservice.config.SecurityConfig;
import org.example.tripservice.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@ActiveProfiles("test")
class RouteRepositoryTest extends BasePostgresRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        route1 = new Route();
        route1.setFromCity("Moscow");
        route1.setToCity("Saint Petersburg");
        route1 = routeRepository.save(route1);

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
    void existsByFromCityAndToCity_NoMatch() {
        boolean exists = routeRepository.existsByFromCityAndToCity("London", "Paris");

        assertFalse(exists);
    }
}
