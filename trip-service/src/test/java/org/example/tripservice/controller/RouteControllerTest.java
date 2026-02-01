package org.example.tripservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.request.RouteUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.exception.RouteNotFoundException;
import org.example.tripservice.service.RouteService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RouteController.class,
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                                                   classes = org.example.tripservice.config.SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RouteControllerTest {

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
    private RouteService routeService;

    private UUID routeId;
    private RouteResponse routeResponse;
    private RouteCreateRequest createRequest;
    private RouteUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        routeResponse = new RouteResponse(routeId, "Moscow", "Saint Petersburg");
        createRequest = new RouteCreateRequest("Moscow", "Saint Petersburg");
        updateRequest = new RouteUpdateRequest("London", "Paris");
    }

    @Test
    void createRoute_Success() throws Exception {
        when(routeService.createRoute(any(RouteCreateRequest.class))).thenReturn(routeResponse);

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId.toString()))
                .andExpect(jsonPath("$.fromCity").value("Moscow"))
                .andExpect(jsonPath("$.toCity").value("Saint Petersburg"));

        verify(routeService).createRoute(any(RouteCreateRequest.class));
    }

    @Test
    void createRoute_ValidationError() throws Exception {
        RouteCreateRequest invalidRequest = new RouteCreateRequest("", "");

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(routeService, never()).createRoute(any());
    }

    @Test
    void getRouteById_Success() throws Exception {
        when(routeService.getRouteById(routeId)).thenReturn(routeResponse);

        mockMvc.perform(get("/routes/{id}", routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId.toString()))
                .andExpect(jsonPath("$.fromCity").value("Moscow"))
                .andExpect(jsonPath("$.toCity").value("Saint Petersburg"));

        verify(routeService).getRouteById(routeId);
    }

    @Test
    void getRouteById_NotFound() throws Exception {
        when(routeService.getRouteById(routeId))
                .thenThrow(new RouteNotFoundException("Route not found"));

        mockMvc.perform(get("/routes/{id}", routeId))
                .andExpect(status().isNotFound());

        verify(routeService).getRouteById(routeId);
    }

    @Test
    void getRoutes_Success() throws Exception {
        Page<RouteResponse> page = new PageImpl<>(
                List.of(routeResponse),
                PageRequest.of(0, 10),
                1
        );

        when(routeService.getRoutes(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/routes")
                        .param("fromCity", "Moscow")
                        .param("toCity", "Saint Petersburg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(routeId.toString()))
                .andExpect(jsonPath("$.content[0].fromCity").value("Moscow"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(routeService).getRoutes(eq("Moscow"), eq("Saint Petersburg"), any());
    }

    @Test
    void getRoutes_WithPagination() throws Exception {
        Page<RouteResponse> page = new PageImpl<>(
                List.of(routeResponse),
                PageRequest.of(0, 5),
                1
        );

        when(routeService.getRoutes(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/routes")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));

        verify(routeService).getRoutes(any(), any(), any());
    }

    @Test
    void updateRoute_Success() throws Exception {
        RouteResponse updatedResponse = new RouteResponse(routeId, "London", "Paris");
        when(routeService.updateRoute(eq(routeId), any(RouteUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/routes/{id}", routeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId.toString()))
                .andExpect(jsonPath("$.fromCity").value("London"))
                .andExpect(jsonPath("$.toCity").value("Paris"));

        verify(routeService).updateRoute(eq(routeId), any(RouteUpdateRequest.class));
    }

    @Test
    void updateRoute_NotFound() throws Exception {
        when(routeService.updateRoute(eq(routeId), any(RouteUpdateRequest.class)))
                .thenThrow(new RouteNotFoundException("Route not found"));

        mockMvc.perform(put("/routes/{id}", routeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(routeService).updateRoute(eq(routeId), any(RouteUpdateRequest.class));
    }

    @Test
    void deleteRouteById_Success() throws Exception {
        doNothing().when(routeService).deleteRouteById(routeId);

        mockMvc.perform(delete("/routes/{id}", routeId))
                .andExpect(status().isNoContent());

        verify(routeService).deleteRouteById(routeId);
    }

    @Test
    void deleteRouteById_NotFound() throws Exception {
        doThrow(new RouteNotFoundException("Route not found"))
                .when(routeService).deleteRouteById(routeId);

        mockMvc.perform(delete("/routes/{id}", routeId))
                .andExpect(status().isNotFound());

        verify(routeService).deleteRouteById(routeId);
    }
}
