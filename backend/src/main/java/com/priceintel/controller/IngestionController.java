package com.priceintel.controller;

import com.priceintel.ingestion.RetailerIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ingestion")
public class IngestionController {
    private final RetailerIngestionService ingestion;
    public IngestionController(RetailerIngestionService ingestion) { this.ingestion = ingestion; }

    @PostMapping("/refresh")
    @Operation(summary = "Run all enabled retailer adapters now")
    public RetailerIngestionService.IngestionReport refresh() {
        return ingestion.refreshAll();
    }
}
