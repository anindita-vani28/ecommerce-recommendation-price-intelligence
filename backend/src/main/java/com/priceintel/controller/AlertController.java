package com.priceintel.controller;

import com.priceintel.service.PriceAlertService;
import com.priceintel.service.PriceAlertService.AlertView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final PriceAlertService alerts;
    public AlertController(PriceAlertService alerts) { this.alerts = alerts; }

    @GetMapping public List<AlertView> watchlist(Authentication user) { return alerts.findAll(user.getName()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertView create(@Valid @RequestBody CreateAlert request, Authentication user) {
        return alerts.create(user.getName(), request.productId(), request.country(), request.targetPrice(), request.channel());
    }

    public record CreateAlert(@NotNull Long productId,
                              @Pattern(regexp = "[A-Za-z]{2}") String country,
                              @NotNull @DecimalMin("0.01") BigDecimal targetPrice,
                              @Pattern(regexp = "email|push") String channel) {}
}
