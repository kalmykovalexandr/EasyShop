package com.easyshop.order.web;

import com.easyshop.order.domain.Order;
import com.easyshop.order.domain.OrderItem;
import com.easyshop.order.domain.OrderRepository;
import com.easyshop.order.domain.OrderItemRepository;
import com.easyshop.order.web.dto.CheckoutDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@RestController
public class OrderController {
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final RestClient http;

    public OrderController(
            OrderRepository orders,
            OrderItemRepository items,
            @Value("${product.base-url}") String base
    ) {
        this.orders = orders;
        this.items = items;
        this.http = RestClient.builder().baseUrl(base).build();
    }

    @GetMapping("/healthz")
    public ApiResponseDto health() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto ready() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/api/orders")
    public List<Map<String, Object>> myOrders(Principal p) {
        String email = p.getName();
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

    @PostMapping("/api/orders/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody CheckoutDto dto, Principal pr) {
        if (dto.items() == null || dto.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Empty cart"));
        }

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
                return switch (ex.getMessage()) {
                    case "NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("message", "Product not found"));
                    case "STOCK_NOT_AVAILABLE" -> ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("message", "Stock not available"));
                    default -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(Map.of("message", "Product service unavailable"));
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
        o.setUserEmail(pr.getName());
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

        return ResponseEntity.ok(Map.of(
                "id", o.getId(),
                "total", o.getTotal(),
                "status", o.getStatus(),
                "items", det
        ));
    }
}
