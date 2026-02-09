package org.example.orderservice.service.client;

import com.booking.inventory.grpc.InventoryServiceGrpc;
import com.booking.inventory.grpc.ReserveSeatsRequest;
import com.booking.inventory.grpc.ReserveSeatsResponse;
import com.booking.inventory.grpc.ConfirmReservationRequest;
import com.booking.inventory.grpc.ReleaseReservationRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.example.orderservice.exception.RetryableInventoryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class InventoryServiceClient {

    private final InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    @Value("${inventory.grpc.timeout-seconds:3}")
    private int timeoutSeconds;

    public InventoryServiceClient(InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub) {
        this.stub = inventoryServiceBlockingStub;
    }

    @Retryable(
            retryFor = RetryableInventoryException.class,
            maxAttemptsExpression = "${inventory.grpc.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${inventory.grpc.retry.delay-ms:200}",
                    multiplierExpression = "${inventory.grpc.retry.multiplier:2.0}"
            )
    )
    public ReserveSeatsResponse reserveSeats(UUID tripId, List<Integer> seats) {
        return executeWithRetry(() ->
                stub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                        .reserveSeats(
                                ReserveSeatsRequest.newBuilder()
                                        .setTripId(tripId.toString())
                                        .addAllSeatNumbers(seats)
                                        .build()
                        )
        );
    }

    @Retryable(
            retryFor = RetryableInventoryException.class,
            maxAttemptsExpression = "${inventory.grpc.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${inventory.grpc.retry.delay-ms:200}",
                    multiplierExpression = "${inventory.grpc.retry.multiplier:2.0}"
            )
    )
    public void confirmReservation(UUID reservationId) {
        runWithRetry(() ->
                stub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                        .confirmReservation(
                                ConfirmReservationRequest.newBuilder()
                                        .setReservationId(reservationId.toString())
                                        .build()
                        )
        );
    }

    @Retryable(
            retryFor = RetryableInventoryException.class,
            maxAttemptsExpression = "${inventory.grpc.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${inventory.grpc.retry.delay-ms:200}",
                    multiplierExpression = "${inventory.grpc.retry.multiplier:2.0}"
            )
    )
    public void releaseReservation(UUID reservationId) {
        runWithRetry(() ->
                stub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                        .releaseReservation(
                                ReleaseReservationRequest.newBuilder()
                                        .setReservationId(reservationId.toString())
                                        .build()
                        )
        );
    }

    private <T> T executeWithRetry(Supplier<T> action) {
        try {
            return action.get();
        } catch (StatusRuntimeException ex) {
            if (isRetryableStatus(ex.getStatus().getCode())) {
                throw new RetryableInventoryException(ex);
            }
            throw ex;
        }
    }

    private void runWithRetry(Runnable action) {
        executeWithRetry(() -> {
            action.run();
            return null;
        });
    }

    private boolean isRetryableStatus(Status.Code code) {
        return code == Status.Code.UNAVAILABLE
                || code == Status.Code.DEADLINE_EXCEEDED
                || code == Status.Code.RESOURCE_EXHAUSTED;
    }
}
