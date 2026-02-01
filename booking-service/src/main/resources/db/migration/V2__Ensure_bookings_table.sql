-- Ensures bookings table exists (e.g. after DB recreate when V1 was marked applied but table missing)
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    trip_id UUID NOT NULL,
    seats_count INT NOT NULL,
    reservation_id UUID,
    reservation_expires_at TIMESTAMP,
    order_id UUID,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
