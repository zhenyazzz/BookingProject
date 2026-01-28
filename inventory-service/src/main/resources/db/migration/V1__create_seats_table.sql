CREATE TABLE seats (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL,
    seat_number INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    last_status_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_seat_trip ON seats(trip_id);
CREATE INDEX idx_seat_status ON seats(status);
