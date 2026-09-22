package com.priceintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "price-intel.ranking")
public record RankingProperties(
        double priceWeight,
        double trustWeight,
        double deliveryWeight,
        double popularityWeight,
        double dealWeight) {

    public RankingProperties {
        double total = priceWeight + trustWeight + deliveryWeight + popularityWeight + dealWeight;
        if (Math.abs(total - 1.0) > 0.0001) {
            throw new IllegalArgumentException("Ranking weights must add up to 1.0");
        }
    }
}
