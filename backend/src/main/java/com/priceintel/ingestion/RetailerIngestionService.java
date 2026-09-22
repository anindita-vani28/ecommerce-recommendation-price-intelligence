package com.priceintel.ingestion;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RetailerIngestionService {
    private static final Logger log = LoggerFactory.getLogger(RetailerIngestionService.class);
    private final List<RetailerCatalogAdapter> adapters;
    private final OfferImportService importer;
    private final MeterRegistry metrics;

    public RetailerIngestionService(List<RetailerCatalogAdapter> adapters,
                                    OfferImportService importer, MeterRegistry metrics) {
        this.adapters = adapters;
        this.importer = importer;
        this.metrics = metrics;
    }

    @CacheEvict(cacheNames = {"product-search", "recommendations"}, allEntries = true)
    public IngestionReport refreshAll() {
        Instant startedAt = Instant.now();
        List<OfferImportService.ImportResult> results = new ArrayList<>();
        List<AdapterFailure> failures = new ArrayList<>();
        for (RetailerCatalogAdapter adapter : adapters) {
            try {
                RetailerOfferBatch batch = adapter.fetchOffers();
                if (!adapter.retailerName().equalsIgnoreCase(batch.retailerName())) {
                    throw new IllegalStateException("Adapter and batch retailer names do not match");
                }
                var result = importer.importBatch(batch);
                results.add(result);
                metrics.counter("priceintel.ingestion.success", "retailer", adapter.retailerName()).increment();
            } catch (RuntimeException exception) {
                log.error("Retailer refresh failed for {}", adapter.retailerName(), exception);
                failures.add(new AdapterFailure(adapter.retailerName(), exception.getMessage()));
                metrics.counter("priceintel.ingestion.failure", "retailer", adapter.retailerName()).increment();
            }
        }
        return new IngestionReport(startedAt, Instant.now(), List.copyOf(results), List.copyOf(failures));
    }

    public record AdapterFailure(String retailer, String message) {}
    public record IngestionReport(Instant startedAt, Instant completedAt,
                                  List<OfferImportService.ImportResult> imports,
                                  List<AdapterFailure> failures) {}
}
