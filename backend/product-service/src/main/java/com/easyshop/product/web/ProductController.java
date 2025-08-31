package com.easyshop.product.web;

import com.easyshop.product.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.*;

@RestController
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository r) {
        repo = r;
    }

    @GetMapping("/healthz")
    public ApiResponseDto h() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto r() {
        return new ApiResponseDto(true, null);
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
    public ResponseEntity<Product> create(@Valid @RequestBody ProductCreateDto b) {
        Product p = Product.builder()
                .name(b.name())
                .description(b.description())
                .price(b.price())
                .stock(b.stock())
                .build();
        return ResponseEntity.status(201).body(repo.save(p));
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<?> upd(@PathVariable Long id, @Valid @RequestBody ProductUpdateDto b) {
        return repo.findById(id).map(p -> {
            p.setName(b.name());
            p.setDescription(b.description());
            p.setPrice(b.price());
            p.setStock(b.stock());
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
    public ResponseEntity<ApiResponseDto> res(@PathVariable Long id, @RequestParam int qty) {
        return repo.findById(id).map(p -> {
            if (p.getStock() < qty)
                return ResponseEntity.status(409).body(new ApiResponseDto(false, "Not enough stock"));
            p.setStock(p.getStock() - qty);
            repo.save(p);
            return ResponseEntity.ok(new ApiResponseDto(true, null));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
