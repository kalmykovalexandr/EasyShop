package com.easyshop.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;

/**
 * Gateway security configuration with OAuth2 Resource Server and CORS support.
 * Handles JWT token validation and CORS preflight requests.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    /**
     * Security filter chain for API Gateway.
     * Configures OAuth2 Resource Server with JWT validation and CORS support.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers("/api/products/**").permitAll()
                        .pathMatchers("/api/cart/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()

                        // Auth endpoints - handled by auth-service
                        .pathMatchers("/api/auth/**").permitAll()

                        // Protected endpoints - require authentication
                        .pathMatchers("/api/purchases/**").authenticated()
                        .pathMatchers("/api/orders/**").authenticated()
                        .pathMatchers("/api/profile/**").authenticated()

                        // Admin endpoints - require ADMIN role
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .build();
    }

    /**
     * CORS configuration for handling browser preflight requests.
     * Must be processed before security to allow browser requests.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Reactive JWT authentication converter to map JWT claims to Spring Security authorities.
     * Maps 'scope' claims to authorities and 'roles' to ROLE_* authorities.
     */
    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthorityPrefix("ROLE_");
        delegate.setAuthoritiesClaimName("scope");

        Converter<Jwt, Flux<GrantedAuthority>> reactiveAdapter = new ReactiveJwtGrantedAuthoritiesConverterAdapter(delegate);

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(reactiveAdapter);
        return converter;
    }

    /**
     * OAuth2 authorized client service for service-to-service communication.
     */
    @Bean
    public ReactiveOAuth2AuthorizedClientService authorizedClientService(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * OAuth2 authorized client manager for managing client credentials.
     */
    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clients,
            ReactiveOAuth2AuthorizedClientService svc) {

        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, svc);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

}
