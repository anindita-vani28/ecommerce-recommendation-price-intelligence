package com.priceintel.ingestion;

import java.time.Instant;
import java.util.List;

public record RetailerOfferBatch(
        String retailerName,
        Instant observedAt,
        boolean completeSnapshot,
        List<ExternalOffer> offers) {
    public RetailerOfferBatch {
        offers = List.copyOf(offers);
    }
}
