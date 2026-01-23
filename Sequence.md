1. Frontend → BookingService: "Хочу забронировать 2 билета"
2. BookingService → Kafka: booking-request (event)
3. OrderService ← Kafka: Создает заказ со статусом PENDING
4. OrderService → Kafka: order-created (id заказа)
5. Frontend ← OrderService: ID заказа для оплаты
6. Frontend → PaymentService: "Оплатить заказ {id}"
7. PaymentService → Stripe: Создает PaymentIntent
8. Frontend ← PaymentService: ClientSecret для оплаты
9. Frontend → Stripe: Оплата картой
10. Stripe → PaymentService (webhook): Результат оплаты
11. PaymentService → OrderService: Подтвердить/отменить заказ
12. OrderService → InventoryService: Списать билеты (если успех)
13. OrderService → Kafka: order-confirmed / order-cancelled
    Как это работает:

Вы регистрируете на стороне Stripe в панели разработчика URL вашего контроллера (например, https://yourdomain.com/api/webhook).

Когда статус платежа меняется (например, на succeeded), Stripe отправляет POST-запрос с событием (Event) на ваш URL.

Ваш код обязан проверить подпись (Stripe-Signature), чтобы убедиться, что запрос действительно пришел от Stripe, а не от злоумышленника.

Вы проверяете тип события (например, payment_intent.succeeded) и только после этого меняете статус заказа в своей базе данных на "Оплачено".

Код в вашем примере как раз это и делает с помощью Webhook.constructEvent().