# MarketLens — E-commerce Recommendation & Price Intelligence

MarketLens is a backend-first portfolio project for comparing country-specific retail offers. It keeps a canonical product catalog, ranks available offers with an explainable and configurable algorithm, stores price history, and lets authenticated users create price alerts.

The stronger version of this idea is not another shopping search engine. It is a **market intelligence platform**: retailer popularity is market-specific, recommendations explain their score, and historical data can later answer “is this actually a deal?” as well as “where should I buy it?”

## What works now

- PostgreSQL-backed products, country offers, price snapshots, and alerts
- Flyway-managed schema and realistic seed data
- Weighted recommendation engine with per-signal score breakdowns
- Market-aware search and retailer popularity signals
- Price-history API
- JWT login and owner-scoped alert endpoints
- Redis-backed search/recommendation caching
- Scheduled, idempotent retailer ingestion through a replaceable adapter SPI
- Price-change snapshots, stale-offer retirement, adapter failure isolation, and ingestion metrics
- Statistical deal verdicts using rolling median, observed lows, and price volatility
- Concurrent-safe alert evaluation with a durable notification outbox
- RFC 9457-style error responses, validation, Actuator, and OpenAPI
- Minimal frontend using the real APIs
- Multi-stage Docker images, Compose stack, tests, and GitHub Actions CI

## Architecture

```text
Browser -> Nginx -> Spring Boot REST API
                         |-- PostgreSQL (source of truth + price history)
                         |-- Redis (query and ranking cache)
                         |-- Ranking engine (configurable weighted-v1)
                         `-- Scheduled ingestion -> retailer adapter SPI
```

The domain and ranking service do not depend on a retailer API. Amazon, Best Buy, MediaMarkt, or affiliate-feed integrations implement `RetailerCatalogAdapter` and publish normalized batches into the same catalog. Each retailer batch is transactional, while failures remain isolated between adapters.

## Run the complete stack

Requirements: Docker with Compose.

```bash
docker compose up --build
```

- Demo UI: http://localhost:3000
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

The database is seeded with Sony headphones in US and DE markets. The UI defaults to the seeded `headphones` search.

## Run the backend locally

Start only infrastructure, then launch Spring Boot:

```bash
docker compose up -d postgres redis
cd backend
mvn spring-boot:run
```

The project targets Java 25 and Spring Boot 3.5.

## API examples

```bash
curl 'http://localhost:8080/api/products/search?query=headphones&country=US'

curl 'http://localhost:8080/api/products/101/recommendations?country=US'

curl 'http://localhost:8080/api/products/101/offers/1001/price-history?days=30'

curl 'http://localhost:8080/api/products/101/offers/1001/deal-insight?days=90'
```

Demo authentication (local development only):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo@priceintel.dev","password":"demo-password"}'
```

Use the returned token as `Authorization: Bearer <token>` for `/api/alerts`.

Trigger all enabled retailer adapters manually with the same token:

```bash
curl -X POST http://localhost:8080/api/admin/ingestion/refresh \
  -H 'Authorization: Bearer <token>'
```

Alert evaluation runs automatically after ingestion and can also be invoked independently:

```bash
curl -X POST http://localhost:8080/api/admin/alerts/evaluate \
  -H 'Authorization: Bearer <token>'

curl http://localhost:8080/api/notifications \
  -H 'Authorization: Bearer <token>'
```

Triggered alerts are one-shot and concurrency-safe: the alert is deactivated and one `PENDING` outbox event is stored in the same transaction. A later email/push delivery adapter can process that event without risking lost or duplicate notifications.

Docker Compose enables a deterministic demo adapter. It updates the seeded Amazon headphone offer once, records the changed price, and is idempotent on later runs. Real adapters are disabled until their API credentials and terms are configured.

## Recommendation model

`weighted-v1` scores each available offer from five explainable signals:

| Signal | Default weight |
| --- | ---: |
| Landed price (item + shipping) | 45% |
| Retailer trust | 20% |
| Delivery speed | 15% |
| Retailer popularity in the selected country | 10% |
| Active deal signal | 10% |

Weights live under `price-intel.ranking` in `application.yml` and must total `1.0`. The API returns the score breakdown and human-readable reasons, which makes later experimentation and A/B testing straightforward.

## Configuration

Important environment variables:

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `REDIS_HOST`, `REDIS_PORT` | Redis connection |
| `JWT_SECRET` | HMAC signing key; must be replaced outside local development |
| `PRICE_REFRESH_DELAY` | Scheduled refresh delay, for example `15m` |
| `OFFER_STALE_AFTER` | Grace period before an unseen offer is retired |
| `DEMO_RETAILER_ENABLED` | Enables the deterministic development adapter |

## Tests and CI

```bash
cd backend
mvn verify
```

The suite covers ranking behavior, full Spring context startup, idempotent offer imports, price-change snapshots, stale-offer retirement, statistical deal classification, and alert/outbox behavior. GitHub Actions runs the same verification on pushes and pull requests.

## Roadmap

1. Add one legitimate public or affiliate retailer API adapter with timeout, retry/backoff, rate limiting, and WireMock contract tests.
2. Build product identity matching across retailers using GTIN/UPC plus normalized brand/model features.
3. Add an email/push delivery worker for pending outbox events with retries and dead-letter handling.
4. Move user identity to PostgreSQL and add OAuth2/OIDC.
5. Add Testcontainers integration tests, load tests, Prometheus/Grafana dashboards, and tracing.
6. Add currency conversion, tax/duty estimates, and regional availability for genuine cross-border landed-cost ranking.

## Repository layout

```text
backend/                 Spring Boot API, migrations, and tests
frontend/                Small API-driven demo UI served by Nginx
.github/workflows/ci.yml CI pipeline
docker-compose.yml       PostgreSQL, Redis, API, and web stack
```

This repository intentionally keeps the frontend small. The engineering depth belongs in ingestion reliability, data modeling, recommendation quality, observability, and testability.
