package org.example.orderservice.service.client;

import com.booking.inventory.grpc.InventoryServiceGrpc;
import com.booking.inventory.grpc.ReserveSeatsRequest;
import com.booking.inventory.grpc.ReserveSeatsResponse;
import com.booking.inventory.grpc.ConfirmReservationRequest;
import com.booking.inventory.grpc.ReleaseReservationRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Service
public class InventoryServiceClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

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
