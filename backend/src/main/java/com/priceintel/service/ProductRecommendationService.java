package com.priceintel.service;

import com.priceintel.config.RankingProperties;
import com.priceintel.domain.Offer;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ProductRecommendationService {
    private final RankingProperties weights;

    public ProductRecommendationService(RankingProperties weights) { this.weights = weights; }

    public List<ScoredOffer> rankOffers(List<Offer> offers) {
        var available = offers.stream().filter(Offer::isAvailability).toList();
        if (available.isEmpty()) return List.of();
        double min = available.stream().mapToDouble(this::totalAsDouble).min().orElse(0);
        double max = available.stream().mapToDouble(this::totalAsDouble).max().orElse(min);
        return available.stream().map(offer -> score(offer, min, max))
                .sorted(Comparator.comparingDouble(ScoredOffer::score).reversed()
                        .thenComparing(scored -> scored.offer().getRetailerName())).toList();
    }

    private ScoredOffer score(Offer offer, double min, double max) {
        double price = max == min ? 100 : 100 * (max - totalAsDouble(offer)) / (max - min);
        double trust = offer.getTrustScore();
        double delivery = Math.max(0, 100 - offer.getDeliveryDays() * 8.0);
        double popularity = offer.getPopularityScore();
        double deal = offer.getDealLabel() == null || offer.getDealLabel().isBlank() ? 40 : 100;
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("price", round(price));
        breakdown.put("trust", round(trust));
        breakdown.put("delivery", round(delivery));
        breakdown.put("marketPopularity", round(popularity));
        breakdown.put("deal", round(deal));
        double total = price * weights.priceWeight() + trust * weights.trustWeight()
                + delivery * weights.deliveryWeight() + popularity * weights.popularityWeight()
                + deal * weights.dealWeight();
        List<String> reasons = List.of(
                price >= 99 ? "Lowest landed price in this market" : "Landed price compared with other available offers",
                "Retailer trust score: " + offer.getTrustScore() + "/100",
                "Market popularity: " + offer.getPopularityScore() + "/100",
                "Estimated delivery in " + offer.getDeliveryDays() + " day(s)");
        return new ScoredOffer(offer, round(total), breakdown, reasons);
    }

    public BigDecimal totalPrice(Offer offer) {
        return offer.getPrice().add(offer.getShippingCost() == null ? BigDecimal.ZERO : offer.getShippingCost());
    }

    private double totalAsDouble(Offer offer) { return totalPrice(offer).doubleValue(); }
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public record ScoredOffer(Offer offer, double score, Map<String, Double> breakdown, List<String> reasons) {}
}
