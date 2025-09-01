package com.easyshop.order.web;

import com.easyshop.order.service.OrderService;
import com.easyshop.order.service.OrderService.ProductNotFoundException;
import com.easyshop.order.service.OrderService.ServiceUnavailableException;
import com.easyshop.order.service.OrderService.StockNotAvailableException;
import com.easyshop.order.web.dto.CheckoutDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/healthz")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    @GetMapping("/readyz")
    public Map<String, Object> ready() {
        return Map.of("ok", true);
    }

    @GetMapping("/api/orders")
    public List<Map<String, Object>> myOrders(Principal p) {
        return service.list(p.getName());
    }

    @PostMapping("/api/orders/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDto dto, Principal pr) {
        if (dto.items() == null || dto.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Empty cart"));
        }
        try {
            return ResponseEntity.ok(service.checkout(dto, pr.getName()));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        } catch (StockNotAvailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Stock not available"));
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", "Product service unavailable"));
        }
    }
}
