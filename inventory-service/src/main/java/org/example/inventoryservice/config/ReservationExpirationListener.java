package org.example.inventoryservice.config;

import org.example.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Component
public class ReservationExpirationListener extends KeyExpirationEventMessageListener {

    private final InventoryService inventoryService;

    @Value("${reservation.trigger-prefix}")
    private String triggerPrefix;

    public ReservationExpirationListener(RedisMessageListenerContainer listenerContainer, InventoryService inventoryService) {
        super(listenerContainer);
        this.inventoryService = inventoryService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.startsWith(triggerPrefix)) {
            String reservationIdStr = expiredKey.replace(triggerPrefix, "");
            try {
                UUID reservationId = UUID.fromString(reservationIdStr);
                inventoryService.handleReservationExpiration(reservationId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid reservation ID in expired key: {}", reservationIdStr);
            }
        }
    }
}
