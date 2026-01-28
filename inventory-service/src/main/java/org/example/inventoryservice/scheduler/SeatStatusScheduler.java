package org.example.inventoryservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatStatusScheduler {

    private final InventoryService inventoryService;

    @Scheduled(fixedRateString = "${reservation.cleanup-interval-ms:60000}")
    public void cleanupExpiredReservations() {
        log.debug("Starting background cleanup of expired reservations...");
        inventoryService.cleanupExpiredReservations();
    }
}
