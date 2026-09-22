package com.priceintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "price-intel.jwt")
public record JwtProperties(String secret, Duration ttl) {}
