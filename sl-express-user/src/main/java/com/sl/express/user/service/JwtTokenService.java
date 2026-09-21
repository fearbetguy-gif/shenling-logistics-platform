package com.sl.express.user.service;

import com.sl.express.user.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    public String createToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.getExpireSeconds())))
                .signWith(signingKey())
                .compact();
    }

    public Long parseUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
