package com.priceintel.ingestion;

import com.priceintel.config.IngestionProperties;
import com.priceintel.domain.Offer;
import com.priceintel.domain.PriceSnapshot;
import com.priceintel.repository.OfferRepository;
import com.priceintel.repository.PriceSnapshotRepository;
import com.priceintel.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class OfferImportService {
    private final ProductRepository products;
    private final OfferRepository offers;
    private final PriceSnapshotRepository snapshots;
    private final IngestionProperties properties;

    public OfferImportService(ProductRepository products, OfferRepository offers,
                              PriceSnapshotRepository snapshots, IngestionProperties properties) {
        this.products = products;
        this.offers = offers;
        this.snapshots = snapshots;
        this.properties = properties;
    }

    @Transactional
    public ImportResult importBatch(RetailerOfferBatch batch) {
        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int rejected = 0;
        List<String> issues = new ArrayList<>();

        for (ExternalOffer external : batch.offers()) {
            String problem = validate(external);
            if (problem != null) {
                rejected++;
                issues.add(external.externalId() + ": " + problem);
                continue;
            }
            var product = products.findBySlug(external.productSlug()).orElse(null);
            if (product == null) {
                rejected++;
                issues.add(external.externalId() + ": unknown product slug " + external.productSlug());
                continue;
            }

            var existing = offers.findByRetailerNameIgnoreCaseAndExternalId(
                    batch.retailerName(), external.externalId());
            Offer offer = existing.orElseGet(Offer::new);
            boolean isNew = existing.isEmpty();
            boolean priceChanged = isNew || !sameAmount(offer.getPrice(), external.price());
            boolean materialChange = isNew || priceChanged || differs(offer, external);

            offer.setProduct(product);
            offer.setRetailerName(batch.retailerName());
            offer.setExternalId(external.externalId());
            offer.setCountryCode(external.countryCode().toUpperCase(Locale.ROOT));
            offer.setPrice(external.price());
            offer.setCurrency(external.currency().toUpperCase(Locale.ROOT));
            offer.setShippingCost(external.shippingCost());
            offer.setTrustScore(external.trustScore());
            offer.setPopularityScore(external.popularityScore());
            offer.setAvailability(external.available());
            offer.setDeliveryDays(external.deliveryDays());
            offer.setDealLabel(external.dealLabel());
            offer.setProductUrl(external.productUrl());
            offer.setLastSeenAt(batch.observedAt());
            offer = offers.save(offer);

            if (priceChanged) {
                PriceSnapshot snapshot = new PriceSnapshot();
                snapshot.setOffer(offer);
                snapshot.setPrice(external.price());
                snapshot.setCapturedAt(batch.observedAt());
                snapshots.save(snapshot);
            }
            if (isNew) created++;
            else if (materialChange) updated++;
            else unchanged++;
        }

        int retired = 0;
        if (batch.completeSnapshot()) {
            Instant cutoff = batch.observedAt().minus(properties.staleAfter());
            List<Offer> stale = offers.findByRetailerNameIgnoreCaseAndAvailabilityTrueAndLastSeenAtBefore(
                    batch.retailerName(), cutoff);
            stale.forEach(offer -> offer.setAvailability(false));
            offers.saveAll(stale);
            retired = stale.size();
        }
        return new ImportResult(batch.retailerName(), created, updated, unchanged, rejected, retired,
                List.copyOf(issues));
    }

    private String validate(ExternalOffer offer) {
        if (blank(offer.productSlug()) || blank(offer.externalId())) return "productSlug and externalId are required";
        if (offer.countryCode() == null || !offer.countryCode().matches("[A-Za-z]{2}")) return "invalid country code";
        if (offer.currency() == null || !offer.currency().matches("[A-Za-z]{3}")) return "invalid currency";
        if (offer.price() == null || offer.price().signum() < 0) return "price must be non-negative";
        if (offer.shippingCost() == null || offer.shippingCost().signum() < 0) return "shipping must be non-negative";
        if (offer.trustScore() < 0 || offer.trustScore() > 100
                || offer.popularityScore() < 0 || offer.popularityScore() > 100) return "scores must be between 0 and 100";
        if (offer.deliveryDays() < 0 || blank(offer.productUrl())) return "delivery and URL are invalid";
        return null;
    }

    private boolean differs(Offer current, ExternalOffer next) {
        return !current.getCountryCode().equalsIgnoreCase(next.countryCode())
                || !current.getCurrency().equalsIgnoreCase(next.currency())
                || !sameAmount(current.getShippingCost(), next.shippingCost())
                || current.getTrustScore() != next.trustScore()
                || current.getPopularityScore() != next.popularityScore()
                || current.isAvailability() != next.available()
                || current.getDeliveryDays() != next.deliveryDays()
                || !Objects.equals(current.getDealLabel(), next.dealLabel())
                || !Objects.equals(current.getProductUrl(), next.productUrl());
    }

    private boolean sameAmount(BigDecimal left, BigDecimal right) {
        return left != null && right != null && left.compareTo(right) == 0;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record ImportResult(String retailer, int created, int updated, int unchanged,
                               int rejected, int retired, List<String> issues) {}
}
