package com.priceintel.service;

import com.priceintel.domain.Offer;
import com.priceintel.config.RankingProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductRecommendationServiceTest {

    @Test
    void shouldRankCheapestOfferHighestWhenOtherSignalsAreEqual() {
        Offer offerA = new Offer();
        offerA.setRetailerName("Amazon");
        offerA.setCountryCode("US");
        offerA.setPrice(new BigDecimal("149.99"));
        offerA.setShippingCost(new BigDecimal("0"));
        offerA.setTrustScore(91);
        offerA.setAvailability(true);
        offerA.setPopularityScore(90);
        offerA.setDeliveryDays(2);

        Offer offerB = new Offer();
        offerB.setRetailerName("Best Buy");
        offerB.setCountryCode("US");
        offerB.setPrice(new BigDecimal("169.99"));
        offerB.setShippingCost(new BigDecimal("0"));
        offerB.setTrustScore(90);
        offerB.setAvailability(true);
        offerB.setPopularityScore(90);
        offerB.setDeliveryDays(2);

        ProductRecommendationService service = new ProductRecommendationService(
                new RankingProperties(.45, .20, .15, .10, .10));
        var rankedOffers = service.rankOffers(List.of(offerA, offerB));

        assertEquals("Amazon", rankedOffers.get(0).offer().getRetailerName());
        assertEquals(2, rankedOffers.size());
    }

    @Test
    void shouldExcludeUnavailableOffers() {
        Offer offer = new Offer();
        offer.setPrice(new BigDecimal("10"));
        offer.setAvailability(false);
        var service = new ProductRecommendationService(new RankingProperties(.45, .20, .15, .10, .10));
        assertEquals(List.of(), service.rankOffers(List.of(offer)));
    }
}
