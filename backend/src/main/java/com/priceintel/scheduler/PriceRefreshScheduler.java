package com.priceintel.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceRefreshScheduler.class);

    @Scheduled(fixedDelayString = "${price-intel.refresh.fixed-delay}")
    public void refreshMarketPrices() {
        log.info("Refreshing retailer pricing data and recommendation cache");
    }
}
