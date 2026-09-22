ALTER TABLE price_alerts
    ADD COLUMN last_triggered_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE notification_outbox (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL REFERENCES price_alerts(id),
    offer_id BIGINT NOT NULL REFERENCES offers(id),
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(255) NOT NULL,
    triggered_price NUMERIC(12, 2) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_notification_alert UNIQUE (alert_id),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notification_owner ON notification_outbox(recipient);
CREATE INDEX idx_notification_status ON notification_outbox(status, next_attempt_at);
