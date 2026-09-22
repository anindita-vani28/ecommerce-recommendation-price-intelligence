package com.priceintel.service;

import com.priceintel.api.ProductDtos.*;
import com.priceintel.domain.Offer;
import com.priceintel.domain.Product;
import com.priceintel.exception.ResourceNotFoundException;
import com.priceintel.repository.PriceSnapshotRepository;
import com.priceintel.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ProductQueryService {
    private final ProductRepository products;
    private final PriceSnapshotRepository snapshots;
    private final ProductRecommendationService ranking;

    public ProductQueryService(ProductRepository products, PriceSnapshotRepository snapshots,
                               ProductRecommendationService ranking) {
        this.products = products;
        this.snapshots = snapshots;
        this.ranking = ranking;
    }

    @Cacheable(cacheNames = "product-search", key = "#query.toLowerCase() + ':' + #country.toUpperCase()")
    public List<ProductSearchResult> search(String query, String country) {
        return products.searchInMarket(query.trim(), country).stream().map(product -> {
            var offers = marketOffers(product, country);
            var best = offers.stream().min(Comparator.comparing(ranking::totalPrice)).orElseThrow();
            return new ProductSearchResult(product.getId(), product.getName(), product.getBrand(),
                    product.getCategory(), ranking.totalPrice(best), best.getCurrency(), offers.size());
        }).toList();
    }

    @Cacheable(cacheNames = "recommendations", key = "#id + ':' + #country.toUpperCase()")
    public RecommendationResponse recommendations(Long id, String country) {
        Product product = products.findInMarket(id, country)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " is unavailable in " + country));
        var ranked = ranking.rankOffers(marketOffers(product, country));
        var results = java.util.stream.IntStream.range(0, ranked.size()).mapToObj(index -> {
            var scored = ranked.get(index);
            Offer offer = scored.offer();
            return new RankedOffer(offer.getId(), index + 1, offer.getRetailerName(), offer.getCountryCode(),
                    offer.getCurrency(), offer.getPrice(), offer.getShippingCost(), ranking.totalPrice(offer),
                    scored.score(), offer.isAvailability(), offer.getDeliveryDays(), offer.getDealLabel(),
                    offer.getProductUrl(), scored.breakdown(), scored.reasons());
        }).toList();
        return new RecommendationResponse(product.getId(), product.getName(), product.getBrand(),
                country.toUpperCase(), "weighted-v1", results);
    }

    public PriceHistoryResponse priceHistory(Long productId, Long offerId, int days) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " was not found"));
        Offer offer = product.getOffers().stream().filter(item -> item.getId().equals(offerId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Offer " + offerId + " was not found for product " + productId));
        var points = snapshots.findByOfferIdAndCapturedAtAfterOrderByCapturedAt(
                        offerId, Instant.now().minus(days, ChronoUnit.DAYS)).stream()
                .map(snapshot -> new PricePoint(snapshot.getCapturedAt(), snapshot.getPrice())).toList();
        return new PriceHistoryResponse(offerId, offer.getRetailerName(), offer.getCurrency(), points);
    }

    private List<Offer> marketOffers(Product product, String country) {
        return product.getOffers().stream().filter(o -> o.getCountryCode().equalsIgnoreCase(country))
                .filter(Offer::isAvailability).toList();
    }
}
