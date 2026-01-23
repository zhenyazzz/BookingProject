INSERT INTO routes (id, from_city, to_city) VALUES

(gen_random_uuid(), 'Минск', 'Гродно'),
(gen_random_uuid(), 'Минск', 'Брест'),
(gen_random_uuid(), 'Минск', 'Витебск'),
(gen_random_uuid(), 'Минск', 'Гомель'),
(gen_random_uuid(), 'Минск', 'Могилев'),
(gen_random_uuid(), 'Минск', 'Барановичи'),
(gen_random_uuid(), 'Минск', 'Пинск'),
(gen_random_uuid(), 'Минск', 'Лида'),
(gen_random_uuid(), 'Минск', 'Новополоцк'),
(gen_random_uuid(), 'Минск', 'Полоцк'),

(gen_random_uuid(), 'Гродно', 'Минск'),
(gen_random_uuid(), 'Гродно', 'Брест'),
(gen_random_uuid(), 'Гродно', 'Лида'),
(gen_random_uuid(), 'Гродно', 'Барановичи'),

(gen_random_uuid(), 'Брест', 'Минск'),
(gen_random_uuid(), 'Брест', 'Гродно'),
(gen_random_uuid(), 'Брест', 'Пинск'),

(gen_random_uuid(), 'Гомель', 'Минск'),
(gen_random_uuid(), 'Гомель', 'Могилев'),
(gen_random_uuid(), 'Гомель', 'Витебск'),

(gen_random_uuid(), 'Витебск', 'Минск'),
(gen_random_uuid(), 'Витебск', 'Гомель'),
(gen_random_uuid(), 'Витебск', 'Полоцк'),

(gen_random_uuid(), 'Могилев', 'Минск'),
(gen_random_uuid(), 'Могилев', 'Гомель'),
(gen_random_uuid(), 'Могилев', 'Витебск'),

(gen_random_uuid(), 'Полоцк', 'Новополоцк'),
(gen_random_uuid(), 'Новополоцк', 'Минск'),
(gen_random_uuid(), 'Лида', 'Минск');


INSERT INTO trips (
    id,
    route_id,
    departure_time,
    arrival_time,
    price,
    total_seats
)
SELECT
    gen_random_uuid(),
    r.id,
    (CURRENT_DATE + day_offset) + time_offset,
    (CURRENT_DATE + day_offset) + time_offset + INTERVAL '3 hours',
    price,
    50
FROM routes r
CROSS JOIN (
    SELECT
        generate_series(0, 6) AS day_offset,
        TIME '08:00' AS time_offset,
        25.00 AS price
    UNION ALL
    SELECT
        generate_series(0, 6),
        TIME '18:00',
        27.00
) s;
