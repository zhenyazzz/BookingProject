package org.example.inventoryservice.grpc;

import org.example.inventoryservice.service.InventoryService;
import org.example.inventoryservice.model.Reservation;
import org.example.inventoryservice.exception.NotEnoughSeatsException;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

import com.booking.inventory.grpc.ReserveSeatsRequest;
import com.booking.inventory.grpc.ReserveSeatsResponse;
import com.booking.inventory.grpc.ConfirmReservationRequest;
import com.booking.inventory.grpc.ReleaseReservationRequest;
import com.booking.inventory.grpc.InventoryServiceGrpc;
import com.google.protobuf.Empty;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcService
        extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService inventoryService;

    @Override
    public void reserveSeats(
            ReserveSeatsRequest request,
            StreamObserver<ReserveSeatsResponse> responseObserver
    ) {

        try {
            Reservation reservation = inventoryService.reserveSeats(
                    UUID.fromString(request.getTripId()),
                    request.getSeatNumbersList()
            );

            ReserveSeatsResponse response =
                    ReserveSeatsResponse.newBuilder()
                            .setReservationId(reservation.reservationId().toString())
                            .setExpiresAt(reservation.expiresAt().toString())
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (NotEnoughSeatsException e) {
            responseObserver.onError(
                Status.FAILED_PRECONDITION
                    .withDescription("Not enough seats")
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void confirmReservation(
        ConfirmReservationRequest request,
        StreamObserver<Empty> responseObserver
    ) {
        inventoryService.confirmReservation(UUID.fromString(request.getReservationId()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void releaseReservation(
        ReleaseReservationRequest request,
        StreamObserver<Empty> responseObserver
    ) {
        inventoryService.releaseReservation(UUID.fromString(request.getReservationId()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
    
}

