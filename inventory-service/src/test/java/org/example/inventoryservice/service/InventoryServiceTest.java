package org.example.inventoryservice.service;

import org.example.inventoryservice.dto.response.SeatResponse;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.model.SeatStatus;
import org.example.inventoryservice.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private org.example.inventoryservice.client.TripServiceClient tripServiceClient;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID tripId;
    private Seat seat;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryService, "dataPrefix", "reservation:");
        ReflectionTestUtils.setField(inventoryService, "triggerPrefix", "trigger:");
        ReflectionTestUtils.setField(inventoryService, "reservationTtlMinutes", 15L);
        tripId = UUID.randomUUID();
        seat = new Seat();
        seat.setId(UUID.randomUUID());
        seat.setTripId(tripId);
        seat.setSeatNumber(1);
        seat.setStatus(SeatStatus.AVAILABLE);
    }

    @Test
    void getSeatsByTripId_whenSeatsExist_returnsList() {
        when(seatRepository.existsByTripId(tripId)).thenReturn(true);
        when(seatRepository.findByTripId(tripId)).thenReturn(List.of(seat));

        List<SeatResponse> result = inventoryService.getSeatsByTripId(tripId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(seat.getSeatNumber(), result.get(0).seatNumber());
        assertEquals(SeatStatus.AVAILABLE, result.get(0).status());
        verify(seatRepository).existsByTripId(tripId);
        verify(seatRepository).findByTripId(tripId);
    }

    @Test
    void getSeatsByTripId_whenNoSeats_createsAndReturns() {
        when(seatRepository.existsByTripId(tripId)).thenReturn(false);
        var tripResponse = new org.example.inventoryservice.client.TripResponse(
                tripId, org.example.kafka.event.BusType.BUS_50, 50);
        when(tripServiceClient.getTripById(tripId)).thenReturn(tripResponse);
        when(seatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(seatRepository.findByTripId(tripId)).thenReturn(List.of(seat));

        List<SeatResponse> result = inventoryService.getSeatsByTripId(tripId);

        assertNotNull(result);
        verify(seatRepository).existsByTripId(tripId);
        verify(tripServiceClient).getTripById(tripId);
        verify(seatRepository).saveAll(anyList());
    }
}
