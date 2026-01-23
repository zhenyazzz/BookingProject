CREATE TABLE bookings (
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

