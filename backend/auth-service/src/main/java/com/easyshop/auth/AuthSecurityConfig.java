package com.easyshop.auth;

import com.easyshop.common.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class AuthSecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(@Value("${jwt.secret}") String secret) {
        return new JwtAuthFilter(secret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity h, JwtAuthFilter jwtAuthFilter) throws Exception {
        h.csrf(cs -> cs.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(r -> r
                        .requestMatchers("/healthz", "/readyz", "/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class);
        return h.build();
    }
}
