package com.familyfood.auth.service.impl;

import com.familyfood.auth.security.UserPrincipal;
import com.familyfood.auth.service.JwtService;
import com.familyfood.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {
    private final AppProperties properties;
    private final SecretKey key;

    @Autowired
    public JwtServiceImpl(AppProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.jwt().ttlMinutes() * 60);
        return Jwts.builder()
                .subject(principal.username())
                .claim("uid", principal.userId())
                .claim("fid", principal.familyId())
                .claim("nickname", principal.nickname())
                .claim("role", principal.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .signWith(key)
                .compact();
    }

    public UserPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new UserPrincipal(
                claims.get("uid", Long.class),
                claims.get("fid", Long.class),
                claims.getSubject(),
                claims.get("nickname", String.class),
                claims.get("role", String.class)
        );
    }
}
