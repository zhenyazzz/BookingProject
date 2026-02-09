package org.example.orderservice.exception;

public class RetryableInventoryException extends RuntimeException {

    public RetryableInventoryException(String message) {
        super(message);
    }

    public RetryableInventoryException(Throwable cause) {
        super(cause);
    }

    public RetryableInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
