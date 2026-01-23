CREATE TABLE routes (
    id UUID PRIMARY KEY,
    from_city VARCHAR(100) NOT NULL,
    to_city VARCHAR(100) NOT NULL,
    CONSTRAINT uk_route_from_to UNIQUE (from_city, to_city)
);

CREATE TABLE trips (
    id UUID PRIMARY KEY,
    route_id UUID NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    total_seats INTEGER NOT NULL,
    CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES routes(id)
);

CREATE INDEX idx_trip_departure ON trips(departure_time);
CREATE INDEX idx_trip_route ON trips(route_id);
CREATE INDEX idx_trip_price ON trips(price);

