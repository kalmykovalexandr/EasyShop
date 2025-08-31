package com.easyshop.order.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Key;
import java.util.List;

import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final Key key;

    public JwtAuthFilter(String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String a = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (a != null && a.startsWith("Bearer ")) {
            try {
                Jws<Claims> j = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(a.substring(7));

                String role = String.valueOf(j.getBody().get("role"));
                var token = new AbstractAuthenticationToken(
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))) {

                    @Override
                    public Object getCredentials() {
                        return "";
                    }

                    @Override
                    public Object getPrincipal() {
                        return j.getBody().getSubject();
                    }
                };
                token.setAuthenticated(true);

                SecurityContextHolder.getContext().setAuthentication(token);
            } catch (Exception ignore) {
                // invalid token
            }
        }
        chain.doFilter(req, res);
    }
}
