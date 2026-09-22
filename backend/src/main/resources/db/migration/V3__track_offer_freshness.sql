ALTER TABLE offers
    ADD COLUMN last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_offer_retailer_freshness
    ON offers(retailer_name, availability, last_seen_at);
