package com.priceintel.ingestion;

import com.priceintel.config.IngestionProperties;
import com.priceintel.domain.Product;
import com.priceintel.repository.OfferRepository;
import com.priceintel.repository.PriceSnapshotRepository;
import com.priceintel.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OfferImportServiceTest {
    @Autowired ProductRepository products;
    @Autowired OfferRepository offers;
    @Autowired PriceSnapshotRepository snapshots;
    private OfferImportService importer;

    @BeforeEach
    void setUp() {
        importer = new OfferImportService(products, offers, snapshots,
                new IngestionProperties(Duration.ofHours(1)));
        Product product = new Product();
        product.setName("Test Headphones");
        product.setSlug("test-headphones");
        product.setBrand("Acme");
        product.setCategory("Electronics");
        products.save(product);
    }

    @Test
    void importsIdempotentlyAndSnapshotsOnlyPriceChanges() {
        Instant firstSeen = Instant.parse("2026-09-22T12:00:00Z");
        var first = importer.importBatch(batch(firstSeen, false, offer("SKU-1", "100.00")));
        var duplicate = importer.importBatch(batch(firstSeen.plusSeconds(60), false, offer("SKU-1", "100.00")));
        var changed = importer.importBatch(batch(firstSeen.plusSeconds(120), false, offer("SKU-1", "90.00")));

        assertThat(first.created()).isEqualTo(1);
        assertThat(duplicate.unchanged()).isEqualTo(1);
        assertThat(changed.updated()).isEqualTo(1);
        assertThat(offers.count()).isEqualTo(1);
        assertThat(snapshots.count()).isEqualTo(2);
    }

    @Test
    void retiresMissingOffersOnlyForCompleteSnapshotsAfterGracePeriod() {
        Instant firstSeen = Instant.parse("2026-09-22T12:00:00Z");
        importer.importBatch(batch(firstSeen, true, offer("SKU-1", "100.00"), offer("SKU-2", "110.00")));

        var result = importer.importBatch(batch(firstSeen.plus(Duration.ofHours(2)), true, offer("SKU-1", "100.00")));

        assertThat(result.retired()).isEqualTo(1);
        assertThat(offers.findByRetailerNameIgnoreCaseAndExternalId("Test Retailer", "SKU-2"))
                .get().extracting(com.priceintel.domain.Offer::isAvailability).isEqualTo(false);
    }

    private RetailerOfferBatch batch(Instant observedAt, boolean complete, ExternalOffer... batchOffers) {
        return new RetailerOfferBatch("Test Retailer", observedAt, complete, List.of(batchOffers));
    }

    private ExternalOffer offer(String externalId, String price) {
        return new ExternalOffer("test-headphones", externalId, "US", new BigDecimal(price), "USD",
                BigDecimal.ZERO, 90, 80, true, 3, null, "https://example.com/" + externalId);
    }
}
