package com.priceintel.ingestion;

/** Boundary implemented by each retailer API, feed, or scraper integration. */
public interface RetailerCatalogAdapter {
    String retailerName();
    RetailerOfferBatch fetchOffers();
}
