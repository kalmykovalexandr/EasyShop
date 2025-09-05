package com.easyshop.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/register", "/api/auth/login", "/api/auth/password-requirements").permitAll()
                        .pathMatchers("/api/products/**").permitAll()
                        .pathMatchers("/api/purchases/**").authenticated()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyExchange().permitAll()
                )
                .build();
    }
}
