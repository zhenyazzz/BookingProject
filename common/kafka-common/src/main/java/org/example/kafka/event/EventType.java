package org.example.kafka.event;

public enum EventType {
    TRIP_CREATED,
    TRIP_CANCELLED,
    TRIP_DEPARTED,
    TRIP_ARRIVED,
    PAYMENT_FAILED,
    PAYMENT_SUCCEEDED,
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELLED
}
