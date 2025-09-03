package com.easyshop.purchase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "product-service", url = "${product.service.url:http://product-service:9002}")
public interface ProductClient {
    
    @GetMapping("/api/products/{id}")
    ProductInfo getProduct(@PathVariable("id") Long id);
    
    record ProductInfo(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock
    ) {}
}
