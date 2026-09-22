package com.priceintel.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_alerts", indexes = @Index(name = "idx_alert_owner", columnList = "owner_username"))
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastTriggeredAt;

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(Instant lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
}
