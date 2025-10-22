package com.example.tasks.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

@Service
public class JwtService {

  @Value("${JWT_SECRET}")
  private String secret;

  private SecretKey key;

  @PostConstruct
  void init() {
    key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generate(String username, Collection<? extends GrantedAuthority> roles) {
    return Jwts.builder()
        .subject(username)
        .claim("roles", roles.stream().map(GrantedAuthority::getAuthority).toList())
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plus(Duration.ofHours(12))))
        .signWith(key)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }
}