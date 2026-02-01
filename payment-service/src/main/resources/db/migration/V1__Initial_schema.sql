CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    payment_intent_id VARCHAR(255),
    client_secret VARCHAR(255),
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP
);
