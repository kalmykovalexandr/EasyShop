package com.easyshop.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${AUTH_URL}")
    private String authUrl;

    @Value("${PRODUCT_URL}")
    private String productUrl;

    @Value("${PURCHASE_URL}")
    private String purchaseUrl;

    @Value("${AUTH_ROUTE}")
    private String authRoute;

    @Value("${PRODUCTS_ROUTE}")
    private String productsRoute;

    @Value("${PURCHASES_ROUTE}")
    private String purchasesRoute;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path(authRoute)
                        .uri(authUrl))
                .route("products", r -> r.path(productsRoute)
                        .uri(productUrl))
                .route("purchases", r -> r.path(purchasesRoute)
                        .uri(purchaseUrl))
                .build();
    }
}
