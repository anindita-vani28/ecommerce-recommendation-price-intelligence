package com.priceintel.ingestion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "price-intel.ingestion", name = "demo-enabled", havingValue = "true")
public class DemoRetailerCatalogAdapter implements RetailerCatalogAdapter {
    @Override
    public String retailerName() { return "Amazon"; }

    @Override
    public RetailerOfferBatch fetchOffers() {
        Instant observedAt = Instant.now();
        return new RetailerOfferBatch(retailerName(), observedAt, false, List.of(
                new ExternalOffer(
                        "sony-wh-1000xm5", "AMZ-US-XM5", "US",
                        new BigDecimal("344.99"), "USD", BigDecimal.ZERO,
                        92, 96, true, 2, "14% below 30-day average",
                        "https://example.com/amazon/xm5"),
                new ExternalOffer(
                        "samsung-galaxy-watch-7", "AMZ-US-GW7", "US",
                        new BigDecimal("249.99"), "USD", BigDecimal.ZERO,
                        92, 96, true, 2, "Limited-time deal",
                        "https://example.com/amazon/gw7")
        ));
    }
}
