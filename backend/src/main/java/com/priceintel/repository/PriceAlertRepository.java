package com.priceintel.repository;

import com.priceintel.domain.PriceAlert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    @EntityGraph(attributePaths = "product")
    List<PriceAlert> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
}
