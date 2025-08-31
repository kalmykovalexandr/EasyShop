package com.easyshop.product.web;

import com.easyshop.product.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.math.*;
import java.util.*;

@RestController
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository r) {
        repo = r;
    }

    @GetMapping("/healthz")
    public Map<String, Object> h() {
        return Map.of("ok", true);
    }

    @GetMapping("/readyz")
    public Map<String, Object> r() {
        return Map.of("ok", true);
    }

    @GetMapping("/api/products")
    public List<Product> list() {
        return repo.findAll().reversed();
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return repo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/admin/products")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> b) {
        Product p = Product.builder().name(String.valueOf(b.get("name"))).description(String.valueOf(b.getOrDefault("description", ""))).price(new BigDecimal(String.valueOf(b.get("price")))).stock(Integer.parseInt(String.valueOf(b.get("stock")))).build();
        return ResponseEntity.status(201).body(repo.save(p));
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<?> upd(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return repo.findById(id).map(p -> {
            p.setName(String.valueOf(b.get("name")));
            p.setDescription(String.valueOf(b.getOrDefault("description", "")));
            p.setPrice(new BigDecimal(String.valueOf(b.get("price"))));
            p.setStock(Integer.parseInt(String.valueOf(b.get("stock"))));
            return ResponseEntity.ok(repo.save(p));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<?> del(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/products/{id}/reserve")
    @Transactional
    public ResponseEntity<?> res(@PathVariable Long id, @RequestParam int qty) {
        return repo.findById(id).map(p -> {
            if (p.getStock() < qty) return ResponseEntity.status(409).body(Map.of("message", "Not enough stock"));
            p.setStock(p.getStock() - qty);
            repo.save(p);
            return ResponseEntity.ok(Map.of("ok", true));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
