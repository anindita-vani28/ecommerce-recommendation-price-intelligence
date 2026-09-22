package com.priceintel.service;

import com.priceintel.domain.NotificationOutbox;
import com.priceintel.repository.NotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationOutboxRepository outbox;
    public NotificationService(NotificationOutboxRepository outbox) { this.outbox = outbox; }

    @Transactional(readOnly = true)
    public List<NotificationView> findAll(String owner) {
        return outbox.findByRecipientOrderByCreatedAtDesc(owner).stream().map(event ->
                new NotificationView(event.getId(), event.getAlert().getId(), event.getOffer().getProduct().getName(),
                        event.getOffer().getRetailerName(), event.getTriggeredPrice(), event.getOffer().getCurrency(),
                        event.getChannel(), event.getStatus(), event.getSubject(), event.getMessage(), event.getCreatedAt()))
                .toList();
    }

    public record NotificationView(Long id, Long alertId, String product, String retailer,
                                   BigDecimal triggeredPrice, String currency, String channel,
                                   NotificationOutbox.Status status, String subject, String message, Instant createdAt) {}
}
