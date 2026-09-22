package com.priceintel.service;

import com.priceintel.api.ProductDtos.DealVerdict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class DealAnalysisService {
    public DealAnalysis analyze(BigDecimal currentPrice, List<BigDecimal> history) {
        List<BigDecimal> prices = history.stream().sorted(Comparator.naturalOrder()).toList();
        if (prices.isEmpty()) {
            return new DealAnalysis(currentPrice, currentPrice, currentPrice, 0, 0, 0,
                    DealVerdict.INSUFFICIENT_HISTORY, "Not enough price history to judge this offer yet");
        }
        BigDecimal median = median(prices);
        BigDecimal lowest = prices.getFirst();
        BigDecimal highest = prices.getLast();
        double discount = median.signum() == 0 ? 0
                : median.subtract(currentPrice).divide(median, 6, RoundingMode.HALF_UP).doubleValue() * 100;
        double mean = prices.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        double variance = prices.stream().mapToDouble(price -> Math.pow(price.doubleValue() - mean, 2))
                .average().orElse(0);
        double volatility = mean == 0 ? 0 : Math.sqrt(variance) / mean * 100;

        DealVerdict verdict;
        String explanation;
        if (prices.size() < 3) {
            verdict = DealVerdict.INSUFFICIENT_HISTORY;
            explanation = "Fewer than three observations are available in this window";
        } else if (currentPrice.compareTo(lowest) <= 0) {
            verdict = DealVerdict.HISTORICAL_LOW;
            explanation = "Current price is the lowest observed in this window";
        } else if (discount >= 10) {
            verdict = DealVerdict.GREAT_DEAL;
            explanation = "Current price is at least 10% below the historical median";
        } else if (discount >= 5) {
            verdict = DealVerdict.GOOD_DEAL;
            explanation = "Current price is at least 5% below the historical median";
        } else if (discount <= -10) {
            verdict = DealVerdict.ABOVE_TYPICAL;
            explanation = "Current price is more than 10% above the historical median";
        } else {
            verdict = DealVerdict.FAIR_PRICE;
            explanation = "Current price is close to its historical median";
        }
        return new DealAnalysis(median, lowest, highest, round(discount), round(volatility), prices.size(),
                verdict, explanation);
    }

    private BigDecimal median(List<BigDecimal> sorted) {
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) return sorted.get(middle);
        return sorted.get(middle - 1).add(sorted.get(middle)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public record DealAnalysis(BigDecimal medianPrice, BigDecimal lowestPrice, BigDecimal highestPrice,
                               double discountFromMedianPercentage, double volatilityPercentage, int sampleSize,
                               DealVerdict verdict, String explanation) {}
}
