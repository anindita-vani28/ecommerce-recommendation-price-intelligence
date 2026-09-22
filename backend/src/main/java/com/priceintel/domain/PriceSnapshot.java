package com.priceintel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_snapshots", indexes = @Index(
        name = "idx_price_snapshot_offer_time", columnList = "offer_id,captured_at"))
public class PriceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    public Long getId() { return id; }
    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
}
