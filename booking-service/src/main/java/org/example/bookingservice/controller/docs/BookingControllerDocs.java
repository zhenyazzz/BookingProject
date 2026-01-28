package org.example.bookingservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;
import org.example.bookingservice.dto.response.CreateBookingResponse;
import org.example.bookingservice.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Бронирование", description = "API для управления бронированиями билетов")
public interface BookingControllerDocs {

    @Operation(summary = "Создать новое бронирование", description = "Создает временное бронирование для указанной поездки")
    @ApiResponse(responseCode = "200", description = "Бронирование успешно создано")
    @ApiResponse(responseCode = "400", description = "Некорректные данные или недостаточно мест")
    ResponseEntity<CreateBookingResponse> createBooking(BookingRequest bookingRequest);

    @Operation(summary = "Получить список всех бронирований", description = "Возвращает страницу с бронированиями с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    ResponseEntity<Page<BookingResponse>> getAllBookings(
            @Parameter(description = "ID пользователя") UUID userId,
            @Parameter(description = "ID поездки") UUID tripId,
            @Parameter(description = "Статус бронирования") BookingStatus status,
            Pageable pageable
    );

    @Operation(summary = "Отменить бронирование", description = "Меняет статус бронирования на CANCELLED")
    @ApiResponse(responseCode = "200", description = "Бронирование успешно отменено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    ResponseEntity<BookingResponse> cancelBooking(UUID id);
}
