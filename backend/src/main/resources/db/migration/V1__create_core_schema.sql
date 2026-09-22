CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    brand VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
);

CREATE INDEX idx_product_name ON products(name);

CREATE TABLE offers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    retailer_name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    shipping_cost NUMERIC(10, 2),
    trust_score INTEGER NOT NULL CHECK (trust_score BETWEEN 0 AND 100),
    popularity_score INTEGER NOT NULL CHECK (popularity_score BETWEEN 0 AND 100),
    availability BOOLEAN NOT NULL DEFAULT TRUE,
    delivery_days INTEGER,
    deal_label VARCHAR(255),
    product_url VARCHAR(1000) NOT NULL,
    CONSTRAINT uk_offer_retailer_external UNIQUE (retailer_name, external_id)
);

CREATE INDEX idx_offer_product_country ON offers(product_id, country_code);

CREATE TABLE price_snapshots (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    price NUMERIC(12, 2) NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_price_snapshot_offer_time ON price_snapshots(offer_id, captured_at);

CREATE TABLE price_alerts (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    owner_username VARCHAR(255) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    target_price NUMERIC(12, 2) NOT NULL,
    channel VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_alert_owner ON price_alerts(owner_username);
