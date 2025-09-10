package com.easyshop.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${AUTH_URL:http://auth-service:9001}")
    private String authUrl;

    @Value("${PRODUCT_URL:http://product-service:9002}")
    private String productUrl;

    @Value("${PURCHASE_URL:http://purchase-service:9003}")
    private String purchaseUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path("/api/auth/**")
                        .uri(authUrl))
                .route("products", r -> r.path("/api/products/**")
                        .uri(productUrl))
                .route("purchases", r -> r.path("/api/purchases/**")
                        .uri(purchaseUrl))
                .build();
    }
}
