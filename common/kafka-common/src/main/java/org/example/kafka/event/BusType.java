package org.example.kafka.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusType {
    MINIBUS(16),
    BUS_40(40),
    BUS_50(50);

    private final int capacity;
}

