package com.easyshop.purchase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Purchase Service security configuration as OAuth2 Resource Server.
 * Validates JWT tokens from Authorization Server and protects endpoints.
 */
@Configuration
@EnableWebSecurity
public class PurchaseSecurityConfig {

    /**
     * Security filter chain for Purchase Service.
     * Configures OAuth2 Resource Server with JWT validation.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // Protected endpoints - require authentication
                        .requestMatchers("/api/purchases/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        
                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/admin/purchases/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     * JWT authentication converter to map JWT claims to Spring Security authorities.
     * Maps 'scope' claims to authorities and 'roles' to ROLE_* authorities.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

}
