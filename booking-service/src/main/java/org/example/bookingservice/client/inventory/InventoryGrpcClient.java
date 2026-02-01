package org.example.bookingservice.client.inventory;

import com.booking.inventory.grpc.InventoryServiceGrpc;
import com.booking.inventory.grpc.ReserveSeatsRequest;
import com.booking.inventory.grpc.ReserveSeatsResponse;
import com.booking.inventory.grpc.ConfirmReservationRequest;
import com.booking.inventory.grpc.ReleaseReservationRequest;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryGrpcClient {

    private final InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    public ReserveSeatsResponse reserveSeats(UUID tripId, List<Integer> seats) {
        return stub.reserveSeats(
                ReserveSeatsRequest.newBuilder()
                        .setTripId(tripId.toString())
                        .addAllSeatNumbers(seats)
                        .build()
        );
    }

    public void confirmReservation(UUID reservationId) {
        stub.confirmReservation(
                ConfirmReservationRequest.newBuilder()
                        .setReservationId(reservationId.toString())
                        .build()
        );
    }

    public void releaseReservation(UUID reservationId) {
        stub.releaseReservation(
                ReleaseReservationRequest.newBuilder()
                        .setReservationId(reservationId.toString())
                        .build()
        );
    }
}

