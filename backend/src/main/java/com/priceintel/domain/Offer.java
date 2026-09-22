package com.priceintel.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "offers", uniqueConstraints = @UniqueConstraint(
        name = "uk_offer_retailer_external", columnNames = {"retailer_name", "external_id"}))
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String retailerName;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private int trustScore;

    @Column(nullable = false)
    private int popularityScore;

    @Column(nullable = false)
    private boolean availability = true;

    @Column
    private int deliveryDays;

    @Column
    private String dealLabel;

    @Column(nullable = false, length = 1000)
    private String productUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getExternalId() { return externalId; }

    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public int getPopularityScore() { return popularityScore; }

    public void setPopularityScore(int popularityScore) { this.popularityScore = popularityScore; }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(int deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getDealLabel() {
        return dealLabel;
    }

    public void setDealLabel(String dealLabel) {
        this.dealLabel = dealLabel;
    }

    public String getProductUrl() { return productUrl; }

    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
}
