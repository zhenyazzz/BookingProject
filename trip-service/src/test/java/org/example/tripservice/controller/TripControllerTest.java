package org.example.tripservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kafka.event.BusType;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.dto.response.TripResponse;
import org.example.tripservice.exception.TripNotFoundException;
import org.example.tripservice.model.TripStatus;
import org.example.tripservice.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TripController.class,
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                                                   classes = org.example.tripservice.config.SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class TripControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ObservationRegistry observationRegistry() {
            return ObservationRegistry.NOOP;
        }

        @Bean
        @Primary
        public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
            return new ObservedAspect(observationRegistry);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    private UUID tripId;
    private UUID routeId;
    private TripResponse tripResponse;
    private TripCreateRequest tripCreateRequest;
    private TripUpdateRequest tripUpdateRequest;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        RouteResponse routeResponse = new RouteResponse(routeId, "Moscow", "Saint Petersburg");
        
        tripResponse = new TripResponse(
                tripId, 
                routeResponse, 
                LocalDateTime.now().plusDays(1), 
                LocalDateTime.now().plusDays(1).plusHours(4), 
                BigDecimal.valueOf(1500), 
                50, 
                TripStatus.SCHEDULED, 
                BusType.BUS_50
        );

        tripCreateRequest = new TripCreateRequest(
                routeId, 
                LocalDateTime.now().plusDays(1), 
                LocalDateTime.now().plusDays(1).plusHours(4), 
                BigDecimal.valueOf(1500), 
                BusType.BUS_50
        );

        tripUpdateRequest = new TripUpdateRequest(
                LocalDateTime.now().plusDays(2), 
                LocalDateTime.now().plusDays(2).plusHours(4), 
                BigDecimal.valueOf(2000), 
                routeId
        );
    }

    @Test
    void createTrip_Success() throws Exception {
        when(tripService.createTrip(any(TripCreateRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.route.fromCity").value("Moscow"));

        verify(tripService).createTrip(any(TripCreateRequest.class));
    }

    @Test
    void getTripById_Success() throws Exception {
        when(tripService.getTripById(tripId)).thenReturn(tripResponse);

        mockMvc.perform(get("/trips/{id}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.route.toCity").value("Saint Petersburg"));

        verify(tripService).getTripById(tripId);
    }

    @Test
    void getTripById_NotFound() throws Exception {
        when(tripService.getTripById(tripId)).thenThrow(new TripNotFoundException("Trip not found"));

        mockMvc.perform(get("/trips/{id}", tripId))
                .andExpect(status().isNotFound());

        verify(tripService).getTripById(tripId);
    }

    @Test
    void getTrips_Success() throws Exception {
        Page<TripResponse> page = new PageImpl<>(List.of(tripResponse), PageRequest.of(0, 10), 1);
        when(tripService.getTrips(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/trips")
                        .param("fromCity", "Moscow")
                        .param("toCity", "Saint Petersburg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(tripId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tripService).getTrips(eq("Moscow"), eq("Saint Petersburg"), any(), any());
    }

    @Test
    void updateTrip_Success() throws Exception {
        when(tripService.updateTrip(eq(tripId), any(TripUpdateRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(put("/trips/{id}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()));

        verify(tripService).updateTrip(eq(tripId), any(TripUpdateRequest.class));
    }

    @Test
    void deleteTripById_Success() throws Exception {
        doNothing().when(tripService).deleteTripById(tripId);

        mockMvc.perform(delete("/trips/{id}", tripId))
                .andExpect(status().isNoContent());

        verify(tripService).deleteTripById(tripId);
    }
}
