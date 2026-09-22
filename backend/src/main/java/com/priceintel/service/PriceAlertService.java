package com.priceintel.service;

import com.priceintel.domain.PriceAlert;
import com.priceintel.exception.ResourceNotFoundException;
import com.priceintel.repository.PriceAlertRepository;
import com.priceintel.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PriceAlertService {
    private final PriceAlertRepository alerts;
    private final ProductRepository products;
    public PriceAlertService(PriceAlertRepository alerts, ProductRepository products) {
        this.alerts = alerts;
        this.products = products;
    }

    @Transactional
    public AlertView create(String owner, Long productId, String country, BigDecimal target, String channel) {
        var product = products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " was not found"));
        var alert = new PriceAlert();
        alert.setProduct(product); alert.setOwnerUsername(owner); alert.setCountryCode(country.toUpperCase());
        alert.setTargetPrice(target); alert.setChannel(channel); alert.setActive(true); alert.setCreatedAt(Instant.now());
        return view(alerts.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertView> findAll(String owner) {
        return alerts.findByOwnerUsernameOrderByCreatedAtDesc(owner).stream().map(this::view).toList();
    }

    private AlertView view(PriceAlert alert) {
        return new AlertView(alert.getId(), alert.getProduct().getId(), alert.getProduct().getName(),
                alert.getCountryCode(), alert.getTargetPrice(), alert.getChannel(), alert.isActive(),
                alert.getCreatedAt(), alert.getLastTriggeredAt());
    }

    public record AlertView(Long id, Long productId, String product, String country, BigDecimal targetPrice,
                            String channel, boolean active, Instant createdAt, Instant lastTriggeredAt) {}
}
