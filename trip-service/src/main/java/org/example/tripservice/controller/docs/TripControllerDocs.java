package org.example.tripservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.TripResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Рейсы", description = "API для управления рейсами (расписание, цены, места)")
public interface TripControllerDocs {

    @Operation(summary = "Создать новый рейс", description = "Создает новый рейс с привязкой к маршруту")
    @ApiResponse(responseCode = "200", description = "Рейс успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    ResponseEntity<TripResponse> createTrip(TripCreateRequest request);

    @Operation(summary = "Получить рейс по ID")
    @ApiResponse(responseCode = "200", description = "Рейс найден")
    @ApiResponse(responseCode = "404", description = "Рейс не найден")
    ResponseEntity<TripResponse> getTripById(UUID id);

    @Operation(summary = "Получить список рейсов", description = "Возвращает страницу с рейсами с возможностью фильтрации по городам и дате")
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    ResponseEntity<Page<TripResponse>> getTrips(
            @Parameter(description = "Город отправления") String fromCity,
            @Parameter(description = "Город прибытия") String toCity,
            @Parameter(description = "Дата отправления (YYYY-MM-DD)") LocalDate date,
            Pageable pageable
    );

    @Operation(summary = "Обновить данные рейса")
    @ApiResponse(responseCode = "200", description = "Рейс успешно обновлен")
    @ApiResponse(responseCode = "404", description = "Рейс не найден")
    ResponseEntity<TripResponse> updateTrip(UUID id, TripUpdateRequest request);

    @Operation(summary = "Удалить рейс по ID")
    @ApiResponse(responseCode = "204", description = "Рейс успешно удален")
    @ApiResponse(responseCode = "404", description = "Рейс не найден")
    ResponseEntity<Void> deleteTripById(UUID id);
}
