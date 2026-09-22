package com.priceintel.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "notification_outbox", uniqueConstraints = @UniqueConstraint(
        name = "uk_notification_alert", columnNames = "alert_id"), indexes = {
        @Index(name = "idx_notification_owner", columnList = "recipient"),
        @Index(name = "idx_notification_status", columnList = "status,next_attempt_at")
})
public class NotificationOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private PriceAlert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal triggeredPrice;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    private Instant processedAt;

    public enum Status { PENDING, SENT, FAILED }

    public Long getId() { return id; }
    public PriceAlert getAlert() { return alert; }
    public void setAlert(PriceAlert alert) { this.alert = alert; }
    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public BigDecimal getTriggeredPrice() { return triggeredPrice; }
    public void setTriggeredPrice(BigDecimal triggeredPrice) { this.triggeredPrice = triggeredPrice; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
