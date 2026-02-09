package org.example.paymentservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(InvalidStripeSignatureException.class)
    public ResponseEntity<String> handleInvalidStripeSignature(InvalidStripeSignatureException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
