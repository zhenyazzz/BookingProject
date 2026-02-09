package org.example.paymentservice.exception;

public class InvalidStripeSignatureException extends RuntimeException {
    public InvalidStripeSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
