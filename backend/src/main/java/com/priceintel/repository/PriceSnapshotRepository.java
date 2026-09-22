package com.priceintel.repository;

import com.priceintel.domain.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    List<PriceSnapshot> findByOfferIdAndCapturedAtAfterOrderByCapturedAt(Long offerId, Instant since);
}
