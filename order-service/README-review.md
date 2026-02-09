# Service Review Report

## Review Timestamp
2025-02-08T00:00:00.000Z

## Service Overview
- **Сервис:** order-service
- **Назначение:** Управление заказами (создание, подтверждение, отмена), оркестрация с inventory (gRPC), реакция на события оплаты/резервации (Kafka).
- **Роль в архитектуре:** Ядро домена «заказ»; потребитель событий payment/inventory/booking; издатель событий order.created / order.confirmed / order.cancelled.

---

## Issues & Fix Plans

### Issue 1 — Критическая уязвимость: подмена userId при создании заказа

**Проблема**
- Контроллер принимает `CreateOrderRequest` с полем `userId` и передаёт его в сервис без проверки. Сервис сохраняет заказ с `request.userId()`. Злоумышленник может создавать заказы от имени любого пользователя.
- **Где:** `OrderController.createOrder()`, `OrderService.createOrder()`.
- **Риск:** Нарушение целостности данных, мошенничество, несанкционированные заказы.

**Fix Plan**
1. Убрать `userId` из `CreateOrderRequest` (или оставить только для внутренних вызовов с отдельным endpoint).
2. В публичном API всегда брать владельца заказа из JWT: `UUID effectiveUserId = SecurityUtils.currentUserId();` и передавать в сервис (отдельный метод или параметр).
3. В сервисе не принимать userId из запроса для пользовательского сценария — только из контекста безопасности.
4. В DTO/документации явно указать: «userId задаётся из токена».
5. Итог: создание заказа возможно только «от имени» текущего аутентифицированного пользователя.

---

### Issue 2 — Нет проверки владельца при get/confirm/cancel

**Проблема**
- `getOrderById(orderId)` возвращает любой заказ по id без проверки, что текущий пользователь — владелец или админ. Аналогично: `confirmOrder` и `cancelOrder` доступны любому аутентифицированному пользователю для любого заказа.
- **Где:** `OrderController` (getOrder, confirmOrder, cancelOrder), `OrderService`.
- **Риск:** Утечка данных (просмотр чужих заказов), несанкционированное подтверждение/отмена.

**Fix Plan**
1. В сервисе для операций «по одному заказу» после загрузки заказа проверять: `order.getUserId().equals(currentUserId) || SecurityUtils.hasRole("ADMIN")`. Если нет — бросать `AccessDeniedException` (403).
2. Контроллер должен передавать в сервис `currentUserId` (из `SecurityUtils.currentUserId()`). Либо вынести проверку в слой сервиса, получая текущего пользователя внутри сервиса (через параметр или SecurityContext).
3. Для `getOrderById`: добавить параметр `UUID currentUserId` в сервис и проверять владельца перед возвратом.
4. Для `confirmOrder`/`cancelOrder`: то же — проверка владельца или роли ADMIN перед изменением статуса.
5. В результате доступ к заказу и смена статуса — только у владельца или админа.

---

### Issue 3 — Исключения через RuntimeException

**Проблема**
- «Order not found» бросается как `RuntimeException`. Это не позволяет клиенту и глобальному обработчику различать 404 и 500, усложняет контракт API и мониторинг.
- **Где:** `OrderService` — getOrderById, confirmOrder, cancelOrder.
- **Риск:** Некорректные HTTP-коды (500 вместо 404), плохая наблюдаемость.

**Fix Plan**
1. Ввести доменное исключение, например `OrderNotFoundException extends RuntimeException`, с полем `orderId` (или общее `ResourceNotFoundException`).
2. Заменить все `throw new RuntimeException("Order not found: " + orderId)` на `throw new OrderNotFoundException(orderId)`.
3. В `@ControllerAdvice` мапить `OrderNotFoundException` в `ResponseEntity.notFound()` (404).
4. Опционально: логировать 404 на уровне WARN, а не ERROR.
5. Итог: стабильный контракт (404 для «не найден») и чёткая иерархия исключений.

---

### Issue 4 — Публикация в Kafka без гарантий доставки

**Проблема**
- `kafkaTemplate.send(...)` вызывается без callback и без проверки результата. При ошибке брокера событие теряется, при этом заказ уже сохранён — возможна рассинхронизация с потребителями (payment, trip и т.д.).
- **Где:** `OrderService` — publishOrderCreatedEvent, publishOrderConfirmedEvent, publishOrderCancelledEvent.
- **Риск:** Потеря событий, нарушение eventual consistency, «зависшие» заказы на стороне потребителей.

**Fix Plan**
1. Использовать `send().get()` (блокирующий вызов с таймаутом) или `CompletableFuture` + callback и при ошибке откатывать транзакцию или помечать заказ как «требующий повторной отправки».
2. Предпочтительно: паттерн Transactional Outbox — сохранять событие в таблицу outbox в той же транзакции, что и заказ; отдельный процесс/шедулер отправляет из outbox в Kafka с идемпотентностью и повторными попытками. Тогда «заказ сохранён» и «событие будет доставлено» согласованы.
3. Если оставаться на прямой отправке: минимум — callback + логирование ошибки и метрика failed_publish; рассмотреть retry с backoff и DLQ.
4. Итог: явная политика «at-least-once» или outbox для согласованности с другими сервисами.

---

### Issue 5 — gRPC-клиент без таймаутов и без закрытия канала

**Проблема**
- `InventoryServiceClient` использует blocking stub без таймаута. Долгий ответ или «висящий» inventory-service блокирует поток и может вызвать каскадные таймауты.
- `ManagedChannel` создаётся в `GrpcConfig`, но нигде не закрывается при остановке приложения — утечка ресурсов.
- **Где:** `GrpcConfig.inventoryChannel()`, `InventoryServiceClient` (confirmReservation, releaseReservation).
- **Риск:** Thread starvation, нестабильная работа под нагрузкой, утечка при рестартах.

**Fix Plan**
1. Задать дедлайны на вызовах: `stub.withDeadlineAfter(3, TimeUnit.SECONDS).confirmReservation(...)` (значение вынести в конфиг, например `inventory.grpc.timeout-seconds`).
2. В `GrpcConfig` добавить `@PreDestroy` метод, вызывающий `inventoryChannel().shutdown()` и при необходимости `awaitTermination()` с таймаутом.
3. Для releaseReservation в `cancelOrderProcess` уже есть try/catch — оставить, но при таймауте/недоступности inventory решить: оставлять заказ отменённым локально и компенсировать позже (событие/ручная сверка) или retry с backoff.
4. Итог: предсказуемые таймауты и корректное освобождение gRPC-канала.

---

### Issue 6 — Дублирование groupId в Kafka-листенерах

**Проблема**
- В `application.yml` задано `spring.kafka.consumer.group-id: order-service`, а в каждом `@KafkaListener` указано `groupId = "order-service"`. Дублирование усложняет смену группы (например, для нового контура потребления) и может привести к расхождениям при копипасте.
- **Где:** PaymentEventListener, BookingEventListener, InventoryEventListener; application.yml.
- **Риск:** Низкий, но поддержка и конфигурирование по окружениям становятся менее очевидными.

**Fix Plan**
1. Удалить атрибут `groupId` из `@KafkaListener` и полагаться на общий `spring.kafka.consumer.group-id`.
2. Либо вынести группу в `${...}` и использовать одну property для всех листенеров этого сервиса.
3. Итог: один источник правды для consumer group.

---

### Issue 7 — Аналитика: JPQL DATE(o.createdAt) и типы

**Проблема**
- В `OrderRepository.getDailyRevenueStats` используется `DATE(o.createdAt)` при типе `Instant`. Поведение зависит от диалекта и таймзоны БД; результат может отличаться от ожидаемого в многозоновом окружении.
- Возврат `List<Object[]>` и приведение `(LocalDate) row[0]`: провайдер может вернуть `java.sql.Date` или другой тип — риск ClassCastException в рантайме.
- **Где:** `OrderRepository.getDailyRevenueStats`, `OrderAnalyticsService.getRevenueStats`.
- **Риск:** Неверная аналитика по дням, падения при смене версии Hibernate/БД.

**Fix Plan**
1. Привести даты к нужной зоне в запросе (например, UTC) или использовать нативную функцию PostgreSQL `DATE(o.createdAt AT TIME ZONE 'UTC')` в `@Query(nativeQuery = true)` с явным маппингом.
2. Либо оставить JPQL, но в сервисе безопасно конвертировать row[0]: проверять тип (Instant, java.sql.Date, LocalDate) и приводить к LocalDate единообразно.
3. Рассмотреть проекцию в DTO/record вместо Object[] (например, интерфейс с getters или конструктор в репозитории).
4. Итог: стабильная по времени и типу агрегация по дням.

---

### Issue 8 — OrderAnalyticsService: возможный duplicate key в toMap

**Проблема**
- `getDailyRevenueStats` возвращает список строк; в сервисе используется `Collectors.toMap(row -> (LocalDate) row[0], ...)`. При смене запроса или при неоднозначности группировки (например, несколько строк на одну дату из-за типа/округления) toMap выбросит `IllegalStateException` из-за дубликата ключа.
- **Где:** `OrderAnalyticsService.getRevenueStats`, строка с `Collectors.toMap`.
- **Риск:** 500 при определённых данных или версиях БД.

**Fix Plan**
1. Использовать `Collectors.toMap(..., (a, b) -> a)` для слияния при дубликате ключа или явно агрегировать по дате (например, суммировать orderCount и revenue).
2. Либо группировать через `Collectors.groupingBy` и затем сводить к одной записи на дату.
3. Итог: устойчивость к дубликатам дат в результате запроса.

---

### Issue 9 — Нет идемпотентности при обработке Kafka-событий

**Проблема**
- При повторной доставке одного и того же события (payment.succeeded, payment.failed, reservation.expired, booking.failed) сервис повторно вызывает confirm/cancel. Для уже подтверждённого/отменённого заказа логика частично идемпотентна (проверка статуса), но confirmOrder вызывает inventoryServiceClient.confirmReservation и публикацию order.confirmed повторно — лишние вызовы и возможные побочные эффекты на стороне inventory и подписчиков.
- **Где:** handlePaymentSucceeded, handlePaymentFailed, handleReservationExpired; BookingEventListener.
- **Риск:** Двойное подтверждение резервации, лишние события, нагрузка.

**Fix Plan**
1. В начале обработчика проверять текущий статус заказа; если уже CONFIRMED/CANCELLED — логировать и выходить без вызова внешних сервисов и без повторной публикации.
2. Рассмотреть идемпотентный ключ: хранить обработанные eventId (или orderId+eventType+version) в кэше/таблице и игнорировать повторные обработки.
3. confirmReservation и releaseReservation на стороне inventory по возможности сделать идемпотентными (по reservationId).
4. Итог: безопасная повторная доставка сообщений без лишних побочных эффектов.

---

### Issue 10 — Логирование и метрики

**Проблема**
- Нет метрик (Micrometer): количество созданных/подтверждённых/отменённых заказов, ошибки Kafka, латенси gRPC. В проде сложно оценивать нагрузку и сбои.
- **Где:** Весь сервис.
- **Риск:** Слабая наблюдаемость в production.

**Fix Plan**
1. Добавить `@Timed` или ручной `Timer` на ключевые операции (createOrder, confirmOrder, cancelOrder, обработчики Kafka).
2. Счётчики: orders_created, orders_confirmed, orders_cancelled; kafka_consumer_errors (по топику/группе); grpc_client_errors.
3. Включить actuator/metrics и экспорт в Prometheus при необходимости.
4. Итог: готовность к мониторингу и алертингу.

---

## Architecture Upgrade Suggestions
- **Границы сервиса:** Чётко разделить «заказ» (order-service) и «резервация мест» (inventory). Текущее разделение в целом корректно; подтверждение/освобождение резервации через gRPC — приемлемо. Рассмотреть событийную альтернативу (inventory публикует reservation.confirmed/reservation.released) для ослабления связности.
- **Асинхронность:** Подтверждение заказа после payment.succeeded можно оставить событийным; для «ручного» confirm от пользователя оставить синхронный вызов inventory с таймаутом и retry.
- **Resilience:** Добавить Resilience4j (circuit breaker + retry) для gRPC-клиента и при необходимости для Kafka producer (если останется прямая отправка). Для Kafka consumer — явная политика retry и DLQ.
- **Наблюдаемость:** Распространение trace-id (SLF4J MDC / Sleuth/Brave) от API и Kafka в логи и метрики; алерты на рост ошибок и латенси.

---

## Performance Improvement Opportunities
- **БД:** Индексы по user_id, trip_id, status, reservation_id уже есть. Для аналитики по датам рассмотреть составной индекс (status, created_at) где status = CONFIRMED, чтобы ускорить расчёт выручки.
- **Память:** Нет очевидных утечек; пагинация в getAllOrders используется корректно.
- **Сеть:** gRPC — уже эффективен; добавить таймауты и ограничение параллельных вызовов при пиках.
- **Конкуррентность:** Kafka-листенеры по умолчанию используют пул потоков; при высокой нагрузке проверить `spring.kafka.listener.concurrency` и backpressure.

---

## Distributed Systems Opportunities
- **Kafka:** Внедрить Transactional Outbox для публикации order.created/confirmed/cancelled; идемпотентные ключи по orderId при отправке; явный retry и DLQ для consumer.
- **Идемпотентность:** Обработчики событий — проверка статуса + опционально хранение обработанных eventId; API confirm/cancel — идемпотентны по смыслу при проверке статуса.
- **Сага:** Сценарий «создание заказа → оплата → подтверждение» уже распределён (order, payment, inventory). При откате (оплата не прошла) отмена заказа и releaseReservation — компенсирующие шаги; при падении после отмены заказа, но до release — добавить повтор release по таймеру или по событию.
- **Кэш:** Для тяжёлой аналитики по датам можно кэшировать результат (например, по ключу startDate+endDate) с коротким TTL; инвалидация при появлении новых заказов — по необходимости.
- **Rate limiting:** На создание заказов и отмену имеет смысл ограничить частоту запросов по userId (на уровне API Gateway или фильтра в сервисе).

---

## Priority Action Queue
1. **Критично:** Исправить безопасность: не доверять userId из запроса при создании заказа; добавить проверку владельца/админа для get/confirm/cancel (Issues 1–2).
2. **Критично:** Ввести доменные исключения и маппинг 404 в глобальном обработчике (Issue 3).
3. **Важно:** Гарантии доставки событий в Kafka: outbox или callback + retry (Issue 4).
4. **Важно:** Таймауты и закрытие gRPC-канала (Issue 5).
5. **Средний приоритет:** Идемпотентность обработки Kafka-событий (Issue 9); надёжность аналитики по датам и toMap (Issues 7–8).
6. **Улучшения:** Убрать дублирование groupId (Issue 6); метрики и логирование (Issue 10).

---

## Skill Growth Targets
- **Безопасность API:** Глубина проверки владельца ресурса, инъекции, доверие к входным данным в микросервисах.
- **Распределённые транзакции и события:** Saga (оркестрация vs choreography), Transactional Outbox, exactly-once семантика и идемпотентность потребителей.
- **gRPC в production:** Таймауты, retry, circuit breaker, управление жизненным циклом каналов.
- **Observability:** Метрики, трейсинг, структурированные логи и алертинг.

---

## Production Readiness Score
**52/100**

- **Обоснование:** Критические дыры в авторизации (подмена userId, доступ к чужим заказам и действиям) не позволяют выставлять выше. Отсутствие гарантий доставки событий и нормальной обработки ошибок (исключения, gRPC) снижают оценку. Плюсы: понятная доменная модель, Flyway, индексы, разделение на слои, частичная идемпотентность по статусу. После устранения пунктов 1–5 приоритетной очереди реалистична оценка 75–82; после добавления outbox, метрик и укрепления аналитики — 85+.
