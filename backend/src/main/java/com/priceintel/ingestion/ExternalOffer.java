package com.priceintel.ingestion;

import java.math.BigDecimal;

public record ExternalOffer(
        String productSlug,
        String externalId,
        String countryCode,
        BigDecimal price,
        String currency,
        BigDecimal shippingCost,
        int trustScore,
        int popularityScore,
        boolean available,
        int deliveryDays,
        String dealLabel,
        String productUrl) {
}
