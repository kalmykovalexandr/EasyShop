package com.easyshop.product.web;

import com.easyshop.product.domain.Product;
import com.easyshop.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.*;

@RestController
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/healthz")
    public ApiResponseDto health() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto ready() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/api/products")
    public List<Product> list() {
        return service.list();
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return service.get(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/admin/products")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductCreateDto b) {
        Product p = service.create(b);
        return ResponseEntity.status(201).body(p);
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateDto b) {
        return service.update(id, b)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/api/admin/products/{id}/reserve")
    public ResponseEntity<ApiResponseDto> reserveStock(@PathVariable Long id, @RequestParam int qty) {
        return switch (service.reserve(id, qty)) {
            case OK -> ResponseEntity.ok(new ApiResponseDto(true, null));
            case NOT_ENOUGH_STOCK -> ResponseEntity.status(409).body(new ApiResponseDto(false, "Not enough stock"));
            case NOT_FOUND -> ResponseEntity.notFound().build();
        };
    }
}
