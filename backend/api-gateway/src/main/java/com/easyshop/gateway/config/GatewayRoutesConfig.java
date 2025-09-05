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
                        .uri("http://auth-service:9001"))
                .route("products", r -> r.path("/api/products/**")
                        .uri("http://product-service:9002"))
                .route("purchases", r -> r.path("/api/purchases/**")
                        .uri("http://purchase-service:9003"))
                .build();
    }
}
