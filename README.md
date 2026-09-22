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
- Scheduled price-refresh boundary ready for retailer adapters
- RFC 9457-style error responses, validation, Actuator, and OpenAPI
- Minimal frontend using the real APIs
- Multi-stage Docker images, Compose stack, tests, and GitHub Actions CI

## Architecture

```text
Browser -> Nginx -> Spring Boot REST API
                         |-- PostgreSQL (source of truth + price history)
                         |-- Redis (query and ranking cache)
                         |-- Ranking engine (configurable weighted-v1)
                         `-- Scheduled ingestion -> retailer adapters (next milestone)
```

The domain and ranking service do not depend on a retailer API. Future Amazon, Best Buy, MediaMarkt, or affiliate-feed integrations should implement adapter interfaces and publish normalized offers into the same catalog.

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
```

Demo authentication (local development only):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo@priceintel.dev","password":"demo-password"}'
```

Use the returned token as `Authorization: Bearer <token>` for `/api/alerts`.

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

## Tests and CI

```bash
cd backend
mvn verify
```

The suite currently covers ranking behavior and full Spring context startup. GitHub Actions runs the same verification on pushes and pull requests.

## Roadmap

1. Define a retailer adapter SPI and add a deterministic mock adapter plus one legitimate public/affiliate API integration.
2. Add idempotent ingestion, retry/backoff, rate limits, stale-offer handling, and cache invalidation.
3. Build product identity matching across retailers using GTIN/UPC plus normalized brand/model features.
4. Add “true deal” detection using rolling median, volatility, and lowest-price windows.
5. Move user identity to PostgreSQL and add OAuth2/OIDC; deliver alerts through an outbox-backed notification worker.
6. Add Testcontainers integration tests, WireMock contract tests, load tests, Prometheus/Grafana dashboards, and tracing.
7. Add currency conversion, tax/duty estimates, and regional availability for genuine cross-border landed-cost ranking.

## Repository layout

```text
backend/                 Spring Boot API, migrations, and tests
frontend/                Small API-driven demo UI served by Nginx
.github/workflows/ci.yml CI pipeline
docker-compose.yml       PostgreSQL, Redis, API, and web stack
```

This repository intentionally keeps the frontend small. The engineering depth belongs in ingestion reliability, data modeling, recommendation quality, observability, and testability.
