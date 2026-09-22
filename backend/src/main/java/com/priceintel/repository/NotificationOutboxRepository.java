package com.priceintel.repository;

import com.priceintel.domain.NotificationOutbox;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    boolean existsByAlertId(Long alertId);

    @EntityGraph(attributePaths = {"alert", "offer", "offer.product"})
    List<NotificationOutbox> findByRecipientOrderByCreatedAtDesc(String recipient);
}
