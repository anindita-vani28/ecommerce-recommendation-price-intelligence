package com.priceintel.service;

import com.priceintel.domain.Offer;
import com.priceintel.domain.PriceAlert;
import com.priceintel.domain.Product;
import com.priceintel.repository.NotificationOutboxRepository;
import com.priceintel.repository.OfferRepository;
import com.priceintel.repository.PriceAlertRepository;
import com.priceintel.repository.ProductRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AlertEvaluationServiceTest {
    @Autowired ProductRepository products;
    @Autowired OfferRepository offers;
    @Autowired PriceAlertRepository alerts;
    @Autowired NotificationOutboxRepository outbox;
    private AlertEvaluationService evaluator;
    private Product product;

    @BeforeEach
    void setUp() {
        evaluator = new AlertEvaluationService(alerts, offers, outbox, new SimpleMeterRegistry());
        product = new Product();
        product.setName("Test Camera");
        product.setSlug("test-camera");
        product.setBrand("Acme");
        product.setCategory("Cameras");
        product = products.save(product);
        offers.save(offer("Fast Shop", "100.00", "0.00"));
        offers.save(offer("Cheap Shop", "95.00", "2.00"));
    }

    @Test
    void createsOneDurableNotificationForTheLowestLandedPrice() {
        PriceAlert alert = alert("100.00");
        var first = evaluator.evaluateActiveAlerts();
        var second = evaluator.evaluateActiveAlerts();

        assertThat(first.checked()).isEqualTo(1);
        assertThat(first.triggered()).isEqualTo(1);
        assertThat(second.triggered()).isZero();
        assertThat(outbox.count()).isEqualTo(1);
        var event = outbox.findAll().getFirst();
        assertThat(event.getOffer().getRetailerName()).isEqualTo("Cheap Shop");
        assertThat(event.getTriggeredPrice()).isEqualByComparingTo("97.00");
        assertThat(alerts.findById(alert.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void leavesAlertActiveWhenTargetHasNotBeenReached() {
        PriceAlert alert = alert("90.00");
        var result = evaluator.evaluateActiveAlerts();

        assertThat(result.triggered()).isZero();
        assertThat(outbox.count()).isZero();
        assertThat(alerts.findById(alert.getId()).orElseThrow().isActive()).isTrue();
    }

    private Offer offer(String retailer, String price, String shipping) {
        Offer offer = new Offer();
        offer.setProduct(product);
        offer.setRetailerName(retailer);
        offer.setExternalId(retailer.replace(" ", "-").toLowerCase());
        offer.setCountryCode("US");
        offer.setPrice(new BigDecimal(price));
        offer.setCurrency("USD");
        offer.setShippingCost(new BigDecimal(shipping));
        offer.setTrustScore(90);
        offer.setPopularityScore(80);
        offer.setAvailability(true);
        offer.setDeliveryDays(2);
        offer.setProductUrl("https://example.com/" + offer.getExternalId());
        offer.setLastSeenAt(Instant.now());
        return offer;
    }

    private PriceAlert alert(String target) {
        PriceAlert alert = new PriceAlert();
        alert.setProduct(product);
        alert.setOwnerUsername("demo@priceintel.dev");
        alert.setCountryCode("US");
        alert.setTargetPrice(new BigDecimal(target));
        alert.setChannel("email");
        alert.setActive(true);
        alert.setCreatedAt(Instant.now());
        return alerts.save(alert);
    }
}
