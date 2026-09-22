package com.priceintel.service;

import com.priceintel.api.ProductDtos.DealInsightResponse;
import com.priceintel.exception.ResourceNotFoundException;
import com.priceintel.repository.OfferRepository;
import com.priceintel.repository.PriceSnapshotRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class DealInsightService {
    private final OfferRepository offers;
    private final PriceSnapshotRepository snapshots;
    private final DealAnalysisService analysis;

    public DealInsightService(OfferRepository offers, PriceSnapshotRepository snapshots,
                              DealAnalysisService analysis) {
        this.offers = offers;
        this.snapshots = snapshots;
        this.analysis = analysis;
    }

    @Cacheable(cacheNames = "deal-insights", key = "#productId + ':' + #offerId + ':' + #days")
    public DealInsightResponse insight(Long productId, Long offerId, int days) {
        var offer = offers.findById(offerId)
                .filter(item -> item.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Offer " + offerId + " was not found for product " + productId));
        var prices = snapshots.findByOfferIdAndCapturedAtAfterOrderByCapturedAt(
                        offerId, Instant.now().minus(days, ChronoUnit.DAYS)).stream()
                .map(snapshot -> snapshot.getPrice()).toList();
        var result = analysis.analyze(offer.getPrice(), prices);
        return new DealInsightResponse(productId, offerId, offer.getRetailerName(), offer.getCurrency(), days,
                offer.getPrice(), result.medianPrice(), result.lowestPrice(), result.highestPrice(),
                result.discountFromMedianPercentage(), result.volatilityPercentage(), result.sampleSize(),
                result.verdict(), result.explanation());
    }
}
