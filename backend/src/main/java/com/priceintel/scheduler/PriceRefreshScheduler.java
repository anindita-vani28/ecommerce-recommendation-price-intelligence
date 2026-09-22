package com.priceintel.scheduler;

import com.priceintel.ingestion.RetailerIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceRefreshScheduler.class);
    private final RetailerIngestionService ingestion;

    public PriceRefreshScheduler(RetailerIngestionService ingestion) {
        this.ingestion = ingestion;
    }

    @Scheduled(fixedDelayString = "${price-intel.refresh.fixed-delay}")
    public void refreshMarketPrices() {
        var report = ingestion.refreshAll();
        log.info("Retailer refresh completed: {} succeeded, {} failed",
                report.imports().size(), report.failures().size());
    }
}
