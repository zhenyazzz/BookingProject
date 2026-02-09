CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    payment_intent_id VARCHAR(255),
    checkout_session_id VARCHAR(255),
    checkout_session_url VARCHAR(2048),
    client_secret VARCHAR(255),
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT uk_payments_order_id UNIQUE (order_id),
    CONSTRAINT uk_payments_payment_intent_id UNIQUE (payment_intent_id)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE INDEX idx_outbox_status_created_at ON outbox_events (status, created_at);
