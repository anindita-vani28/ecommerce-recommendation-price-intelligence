package com.priceintel.repository;

import com.priceintel.domain.PriceAlert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import jakarta.persistence.LockModeType;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    @EntityGraph(attributePaths = "product")
    List<PriceAlert> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from PriceAlert a join fetch a.product where a.active = true order by a.id")
    List<PriceAlert> findActiveForEvaluation();
}
