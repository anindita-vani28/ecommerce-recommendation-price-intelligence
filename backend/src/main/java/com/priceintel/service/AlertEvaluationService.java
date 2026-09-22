package com.priceintel.service;

import com.priceintel.domain.NotificationOutbox;
import com.priceintel.domain.Offer;
import com.priceintel.repository.NotificationOutboxRepository;
import com.priceintel.repository.OfferRepository;
import com.priceintel.repository.PriceAlertRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;

@Service
public class AlertEvaluationService {
    private final PriceAlertRepository alerts;
    private final OfferRepository offers;
    private final NotificationOutboxRepository outbox;
    private final MeterRegistry metrics;

    public AlertEvaluationService(PriceAlertRepository alerts, OfferRepository offers,
                                  NotificationOutboxRepository outbox, MeterRegistry metrics) {
        this.alerts = alerts;
        this.offers = offers;
        this.outbox = outbox;
        this.metrics = metrics;
    }

    @Transactional
    public EvaluationReport evaluateActiveAlerts() {
        int checked = 0;
        int triggered = 0;
        Instant now = Instant.now();
        for (var alert : alerts.findActiveForEvaluation()) {
            checked++;
            if (outbox.existsByAlertId(alert.getId())) {
                alert.setActive(false);
                continue;
            }
            Offer best = offers.findByProductIdAndCountryCodeIgnoreCaseAndAvailabilityTrue(
                            alert.getProduct().getId(), alert.getCountryCode()).stream()
                    .min(Comparator.comparing(this::landedPrice)).orElse(null);
            if (best == null) continue;
            BigDecimal currentPrice = landedPrice(best);
            if (currentPrice.compareTo(alert.getTargetPrice()) > 0) continue;

            NotificationOutbox event = new NotificationOutbox();
            event.setAlert(alert);
            event.setOffer(best);
            event.setRecipient(alert.getOwnerUsername());
            event.setChannel(alert.getChannel());
            event.setTriggeredPrice(currentPrice);
            event.setSubject("Price target reached: " + alert.getProduct().getName());
            event.setMessage(best.getRetailerName() + " now offers " + alert.getProduct().getName()
                    + " for " + currentPrice + " " + best.getCurrency()
                    + ", at or below your target of " + alert.getTargetPrice() + ".");
            event.setStatus(NotificationOutbox.Status.PENDING);
            event.setAttempts(0);
            event.setCreatedAt(now);
            event.setNextAttemptAt(now);
            outbox.save(event);

            alert.setActive(false);
            alert.setLastTriggeredAt(now);
            triggered++;
            metrics.counter("priceintel.alerts.triggered", "channel", alert.getChannel()).increment();
        }
        return new EvaluationReport(checked, triggered, now);
    }

    private BigDecimal landedPrice(Offer offer) {
        return offer.getPrice().add(offer.getShippingCost() == null ? BigDecimal.ZERO : offer.getShippingCost());
    }

    public record EvaluationReport(int checked, int triggered, Instant evaluatedAt) {}
}
