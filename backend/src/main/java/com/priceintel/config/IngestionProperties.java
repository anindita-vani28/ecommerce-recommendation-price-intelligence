package com.priceintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "price-intel.ingestion")
public record IngestionProperties(Duration staleAfter) {
    public IngestionProperties {
        if (staleAfter == null || staleAfter.isNegative() || staleAfter.isZero()) {
            throw new IllegalArgumentException("price-intel.ingestion.stale-after must be positive");
        }
    }
}
