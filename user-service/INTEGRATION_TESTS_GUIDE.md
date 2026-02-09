# Integration Tests Guide (user-service)

Короткий алгоритм, как писать интеграционные тесты для `user-service`.

## 1) Выбери уровень теста
- Контроллер + сервис + БД = интеграционный.
- Контроллер без БД = web‑test (лучше `@WebMvcTest`).

## 2) Наследуйся от `BaseIntegrationTest`
- Там уже поднимается PostgreSQL через Testcontainers.
- Используй `@SpringBootTest(webEnvironment = RANDOM_PORT)`.

## 3) Подготовь тестовые данные
- Перед тестом очищай БД.
- Сохраняй сущности через репозитории.

## 4) Настрой security для теста
- Для проверки безопасности лучше оставить реальный `SecurityFilterChain`.
- Подмени `JwtDecoder` в тестах:
  - создай `@TestConfiguration`,
  - добавь `@Primary` bean `JwtDecoder`,
  - возвращай `Jwt` с `realm_access.roles`.
- В запросах передавай `Authorization: Bearer <token>`.

## 5) Сделай HTTP‑вызов
- Используй `TestRestTemplate`:
  - `getForEntity()` для GET,
  - `exchange()` для DELETE/PUT.

## 6) Проверь результат
- Статус ответа (`200`, `204`, `404`).
- Тело ответа (через `ObjectMapper` или строковые проверки).
- Состояние БД после вызова.

## 7) Оставь тест устойчивым
- Не используй реальные внешние сервисы.
- Данные должны быть локальными и предсказуемыми.

