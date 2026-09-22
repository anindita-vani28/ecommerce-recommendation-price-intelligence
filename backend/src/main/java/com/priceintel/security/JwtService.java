package com.priceintel.security;

import com.priceintel.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;
    public JwtService(JwtProperties properties) { this.properties = properties; }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.ttl()))).signWith(signingKey()).compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        try { return claims(token).getExpiration().after(new Date()); }
        catch (RuntimeException exception) { return false; }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }
}
