package com.priceintel.repository;

import com.priceintel.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByRetailerNameIgnoreCaseAndExternalId(String retailerName, String externalId);

    List<Offer> findByRetailerNameIgnoreCaseAndAvailabilityTrueAndLastSeenAtBefore(
            String retailerName, Instant cutoff);

    List<Offer> findByProductIdAndCountryCodeIgnoreCaseAndAvailabilityTrue(Long productId, String countryCode);
}
