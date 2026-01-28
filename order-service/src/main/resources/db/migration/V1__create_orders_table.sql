CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    trip_id UUID NOT NULL,
    seats_count INTEGER NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    reservation_id UUID,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_trip ON orders(trip_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_reservation ON orders(reservation_id);
