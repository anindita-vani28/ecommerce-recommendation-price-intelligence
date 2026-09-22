package com.priceintel.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public final class ProductDtos {
    private ProductDtos() {}

    public record ProductSearchResult(
            Long id, String name, String brand, String category,
            BigDecimal bestTotalPrice, String currency, int offerCount) implements Serializable {}

    public record RankedOffer(
            Long offerId, int rank, String retailer, String country, String currency,
            BigDecimal itemPrice, BigDecimal shipping, BigDecimal totalPrice,
            double score, boolean available, int deliveryDays, String dealLabel,
            String productUrl, Map<String, Double> scoreBreakdown, List<String> explanation) implements Serializable {}

    public record RecommendationResponse(
            Long productId, String product, String brand, String country,
            String algorithm, List<RankedOffer> recommendations) implements Serializable {}

    public record PricePoint(Instant capturedAt, BigDecimal price) implements Serializable {}

    public record PriceHistoryResponse(
            Long offerId, String retailer, String currency, List<PricePoint> history) implements Serializable {}
}
