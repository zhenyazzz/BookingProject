package org.example.bookingservice.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotEnoughCapacityException extends RuntimeException{
    public NotEnoughCapacityException(String message) {
        super(message);
    }
}
