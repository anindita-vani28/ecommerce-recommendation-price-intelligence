package com.priceintel.controller;

import com.priceintel.api.ProductDtos.*;
import com.priceintel.service.ProductQueryService;
import com.priceintel.service.DealInsightService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductQueryService products;
    private final DealInsightService deals;
    public ProductController(ProductQueryService products, DealInsightService deals) {
        this.products = products;
        this.deals = deals;
    }

    @GetMapping("/search")
    @Operation(summary = "Search the catalog in a country market")
    public List<ProductSearchResult> search(
            @RequestParam @Size(min = 2, max = 100) String query,
            @RequestParam(defaultValue = "US") @Pattern(regexp = "[A-Za-z]{2}") String country) {
        return products.search(query, country);
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Rank offers and explain every score")
    public RecommendationResponse recommendations(@PathVariable Long id,
            @RequestParam(defaultValue = "US") @Pattern(regexp = "[A-Za-z]{2}") String country) {
        return products.recommendations(id, country);
    }

    @GetMapping("/{productId}/offers/{offerId}/price-history")
    @Operation(summary = "Return time-series price history for an offer")
    public PriceHistoryResponse priceHistory(@PathVariable Long productId, @PathVariable Long offerId,
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return products.priceHistory(productId, offerId, days);
    }

    @GetMapping("/{productId}/offers/{offerId}/deal-insight")
    @Operation(summary = "Evaluate whether the current offer is a true deal")
    public DealInsightResponse dealInsight(@PathVariable Long productId, @PathVariable Long offerId,
            @RequestParam(defaultValue = "90") @Min(7) @Max(365) int days) {
        return deals.insight(productId, offerId, days);
    }
}
