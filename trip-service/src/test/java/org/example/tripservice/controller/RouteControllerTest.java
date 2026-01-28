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
import org.example.tripservice.config.WebMvcTestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RouteController.class)
@ActiveProfiles("test")
class RouteControllerTest {

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
    @WithMockUser(roles = "ADMIN")
    void createRoute_Success() throws Exception {
        when(routeService.createRoute(any(RouteCreateRequest.class))).thenReturn(routeResponse);

        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId.toString()))
                .andExpect(jsonPath("$.fromCity").value("Moscow"))
                .andExpect(jsonPath("$.toCity").value("Saint Petersburg"));

        verify(routeService).createRoute(any(RouteCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRoute_Forbidden() throws Exception {
        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(routeService, never()).createRoute(any());
    }

    @Test
    void createRoute_Unauthorized() throws Exception {
        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(routeService, never()).createRoute(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoute_ValidationError() throws Exception {
        RouteCreateRequest invalidRequest = new RouteCreateRequest("", "");

        mockMvc.perform(post("/routes")
                        .with(csrf())
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
    @WithMockUser(roles = "ADMIN")
    void updateRoute_Success() throws Exception {
        RouteResponse updatedResponse = new RouteResponse(routeId, "London", "Paris");
        when(routeService.updateRoute(eq(routeId), any(RouteUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/routes/{id}", routeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId.toString()))
                .andExpect(jsonPath("$.fromCity").value("London"))
                .andExpect(jsonPath("$.toCity").value("Paris"));

        verify(routeService).updateRoute(eq(routeId), any(RouteUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateRoute_Forbidden() throws Exception {
        mockMvc.perform(put("/routes/{id}", routeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(routeService, never()).updateRoute(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoute_NotFound() throws Exception {
        when(routeService.updateRoute(eq(routeId), any(RouteUpdateRequest.class)))
                .thenThrow(new RouteNotFoundException("Route not found"));

        mockMvc.perform(put("/routes/{id}", routeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(routeService).updateRoute(eq(routeId), any(RouteUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRouteById_Success() throws Exception {
        doNothing().when(routeService).deleteRouteById(routeId);

        mockMvc.perform(delete("/routes/{id}", routeId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(routeService).deleteRouteById(routeId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteRouteById_Forbidden() throws Exception {
        mockMvc.perform(delete("/routes/{id}", routeId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(routeService, never()).deleteRouteById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRouteById_NotFound() throws Exception {
        doThrow(new RouteNotFoundException("Route not found"))
                .when(routeService).deleteRouteById(routeId);

        mockMvc.perform(delete("/routes/{id}", routeId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(routeService).deleteRouteById(routeId);
    }
}
