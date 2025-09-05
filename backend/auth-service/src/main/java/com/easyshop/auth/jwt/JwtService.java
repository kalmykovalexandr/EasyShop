package com.easyshop.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.security.*;
import java.util.*;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    String secret;
    @Value("${jwt.ttlMinutes}")
    long ttl;
    @Value("${jwt.refreshTtlMinutes:1440}") // 24 hours default
    long refreshTtl;

    Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String create(String sub, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttl * 60 * 1000);
        
        return Jwts.builder()
                .subject(sub)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key())
                .compact();
    }

    public String createRefreshToken(String sub) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTtl * 60 * 1000);
        
        return Jwts.builder()
                .subject(sub)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
