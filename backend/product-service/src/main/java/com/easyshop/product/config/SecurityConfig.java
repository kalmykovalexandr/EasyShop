package com.easyshop.product.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.security.Key;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    String secret;

    Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Bean
    SecurityFilterChain c(HttpSecurity h) throws Exception {
        h.csrf(cs -> cs.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(r -> r
                        .requestMatchers("/healthz", "/readyz", "/api/products/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore((req, res, chain) -> auth(req, res, chain), BasicAuthenticationFilter.class);
        return h.build();
    }

    void auth(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException {
        String a = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (a != null && a.startsWith("Bearer ")) {
            try {
                Jws<Claims> j = Jwts.parserBuilder()
                        .setSigningKey(key())
                        .build()
                        .parseClaimsJws(a.substring(7));
                String role = String.valueOf(j.getBody().get("role"));
                var token = new AbstractAuthenticationToken(List.of(new SimpleGrantedAuthority("ROLE_" + role))) {
                    @Override public Object getCredentials() { return ""; }
                    @Override public Object getPrincipal() { return j.getBody().getSubject(); }
                    { setAuthenticated(true); }
                };
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(token);
            } catch (Exception ignore) { }
        }
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
