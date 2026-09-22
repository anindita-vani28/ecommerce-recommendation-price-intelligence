INSERT INTO products (id, name, slug, brand, category) VALUES
  (101, 'WH-1000XM5 Noise Cancelling Headphones', 'sony-wh-1000xm5', 'Sony', 'Electronics'),
  (102, 'Galaxy Watch 7', 'samsung-galaxy-watch-7', 'Samsung', 'Wearables');

INSERT INTO offers (id, product_id, retailer_name, external_id, country_code, price, currency,
                    shipping_cost, trust_score, popularity_score, availability, delivery_days, deal_label, product_url) VALUES
  (1001, 101, 'Amazon', 'AMZ-US-XM5', 'US', 349.99, 'USD', 0, 92, 96, TRUE, 2, '12% below 30-day average', 'https://example.com/amazon/xm5'),
  (1002, 101, 'Best Buy', 'BB-US-XM5', 'US', 379.99, 'USD', 0, 94, 88, TRUE, 1, NULL, 'https://example.com/bestbuy/xm5'),
  (1003, 101, 'MediaMarkt', 'MM-DE-XM5', 'DE', 329.00, 'EUR', 4.99, 89, 93, TRUE, 3, 'Weekend deal', 'https://example.com/mediamarkt/xm5'),
  (1004, 102, 'Amazon', 'AMZ-US-GW7', 'US', 249.99, 'USD', 0, 92, 96, TRUE, 2, 'Limited-time deal', 'https://example.com/amazon/gw7');

INSERT INTO price_snapshots (offer_id, price, captured_at) VALUES
  (1001, 399.99, CURRENT_TIMESTAMP - INTERVAL '28 days'),
  (1001, 389.99, CURRENT_TIMESTAMP - INTERVAL '21 days'),
  (1001, 369.99, CURRENT_TIMESTAMP - INTERVAL '14 days'),
  (1001, 349.99, CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (1002, 399.99, CURRENT_TIMESTAMP - INTERVAL '28 days'),
  (1002, 379.99, CURRENT_TIMESTAMP - INTERVAL '1 day');

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
SELECT setval('offers_id_seq', (SELECT MAX(id) FROM offers));
