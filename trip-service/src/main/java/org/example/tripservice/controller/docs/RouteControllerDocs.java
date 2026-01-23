package org.example.tripservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.request.RouteUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Маршруты", description = "API для управления маршрутами (города отправления и прибытия)")
public interface RouteControllerDocs {

    @Operation(summary = "Создать новый маршрут", description = "Создает новый уникальный маршрут между двумя городами")
    @ApiResponse(responseCode = "200", description = "Маршрут успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    ResponseEntity<RouteResponse> createRoute(RouteCreateRequest request);

    @Operation(summary = "Получить маршрут по ID")
    @ApiResponse(responseCode = "200", description = "Маршрут найден")
    @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    ResponseEntity<RouteResponse> getRouteById(UUID id);

    @Operation(summary = "Получить список маршрутов", description = "Возвращает страницу с маршрутами с возможностью фильтрации по городам")
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    ResponseEntity<Page<RouteResponse>> getRoutes(
            @Parameter(description = "Город отправления") String fromCity,
            @Parameter(description = "Город прибытия") String toCity,
            Pageable pageable
    );

    @Operation(summary = "Обновить существующий маршрут")
    @ApiResponse(responseCode = "200", description = "Маршрут успешно обновлен")
    @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    ResponseEntity<RouteResponse> updateRoute(UUID id, RouteUpdateRequest request);

    @Operation(summary = "Удалить маршрут по ID")
    @ApiResponse(responseCode = "204", description = "Маршрут успешно удален")
    @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    ResponseEntity<Void> deleteRouteById(UUID id);
}
