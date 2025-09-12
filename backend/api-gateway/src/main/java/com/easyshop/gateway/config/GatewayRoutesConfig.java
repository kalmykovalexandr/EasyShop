package com.easyshop.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${PRODUCT_SERVICE_URL}")
    private String productServiceUrl;

    @Value("${PURCHASE_SERVICE_URL}")
    private String purchaseServiceUrl;

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
                        .uri(authServiceUrl))
                .route("products", r -> r.path(productsRoute)
                        .uri(productServiceUrl))
                .route("purchases", r -> r.path(purchasesRoute)
                        .uri(purchaseServiceUrl))
                .build();
    }
}
