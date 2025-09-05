package com.easyshop.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path("/api/auth/**")
                        .uri("lb://auth-service"))
                .route("products", r -> r.path("/api/products/**")
                        .uri("lb://product-service"))
                .route("purchases", r -> r.path("/api/purchases/**")
                        .uri("lb://purchase-service"))
                .build();
    }
}
