package com.easyshop.order.service;

import com.easyshop.order.domain.Order;
import com.easyshop.order.domain.OrderItem;
import com.easyshop.order.domain.OrderItemRepository;
import com.easyshop.order.domain.OrderRepository;
import com.easyshop.order.web.dto.CheckoutDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final RestClient http;

    public OrderService(OrderRepository orders, OrderItemRepository items, @Value("${product.base-url}") String base) {
        this.orders = orders;
        this.items = items;
        this.http = RestClient.builder().baseUrl(base).build();
    }

    public List<Map<String, Object>> list(String email) {
        return orders.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(o -> Map.of(
                        "id", o.getId(),
                        "total", o.getTotal(),
                        "status", o.getStatus(),
                        "items", o.getItems().stream().map(i -> Map.of(
                                "productId", i.getProductId(),
                                "name", i.getName(),
                                "price", i.getPrice(),
                                "quantity", i.getQuantity()
                        )).toList()
                ))
                .toList();
    }

    @Transactional
    public Map<String, Object> checkout(CheckoutDto dto, String email) {
        List<Map<String, Object>> det = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutDto.Item it : dto.items()) {
            Map<String, Object> prod;
            try {
                prod = http.get()
                        .uri("/api/products/{id}", it.productId())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                            throw new RestClientException("NOT_FOUND");
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                            throw new RestClientException("SERVICE_UNAVAILABLE");
                        })
                        .body(Map.class);

                http.post()
                        .uri("/api/admin/products/{id}/reserve?qty={q}", it.productId(), it.quantity())
                        .retrieve()
                        .onStatus(status -> status.value() == 409, (req, res) -> {
                            throw new RestClientException("STOCK_NOT_AVAILABLE");
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                            throw new RestClientException("SERVICE_UNAVAILABLE");
                        })
                        .toBodilessEntity();
            } catch (RestClientException ex) {
                throw switch (ex.getMessage()) {
                    case "NOT_FOUND" -> new ProductNotFoundException();
                    case "STOCK_NOT_AVAILABLE" -> new StockNotAvailableException();
                    default -> new ServiceUnavailableException();
                };
            }

            BigDecimal price = new BigDecimal(String.valueOf(prod.get("price")));
            total = total.add(price.multiply(BigDecimal.valueOf(it.quantity())));
            det.add(Map.of(
                    "productId", it.productId(),
                    "name", prod.get("name"),
                    "price", price,
                    "quantity", it.quantity()
            ));
        }

        Order o = new Order();
        o.setUserEmail(email);
        o.setTotal(total);
        o.setStatus("CREATED");
        orders.save(o);

        for (var d : det) {
            OrderItem oi = new OrderItem(
                    null,
                    o,
                    ((Number) d.get("productId")).longValue(),
                    String.valueOf(d.get("name")),
                    (BigDecimal) d.get("price"),
                    ((Number) d.get("quantity")).intValue()
            );
            items.save(oi);
            o.getItems().add(oi);
        }

        return Map.of(
                "id", o.getId(),
                "total", o.getTotal(),
                "status", o.getStatus(),
                "items", det
        );
    }

    public static class ProductNotFoundException extends RuntimeException {}
    public static class StockNotAvailableException extends RuntimeException {}
    public static class ServiceUnavailableException extends RuntimeException {}
}
