package com.easyshop.product.service;

import com.easyshop.product.domain.Product;
import com.easyshop.product.domain.ProductRepository;
import com.easyshop.product.web.ProductCreateDto;
import com.easyshop.product.web.ProductUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> list() {
        return repo.findAll().reversed();
    }

    public Optional<Product> get(Long id) {
        return repo.findById(id);
    }

    public Product create(ProductCreateDto b) {
        Product p = Product.builder()
                .name(b.name())
                .description(b.description())
                .price(b.price())
                .stock(b.stock())
                .build();
        return repo.save(p);
    }

    public Optional<Product> update(Long id, ProductUpdateDto b) {
        return repo.findById(id).map(p -> {
            p.setName(b.name());
            p.setDescription(b.description());
            p.setPrice(b.price());
            p.setStock(b.stock());
            return repo.save(p);
        });
    }

    public boolean delete(Long id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
        return true;
    }

    @Transactional
    public ReserveResult reserve(Long id, int qty) {
        return repo.findById(id).map(p -> {
            if (p.getStock() < qty) {
                return ReserveResult.NOT_ENOUGH_STOCK;
            }
            p.setStock(p.getStock() - qty);
            repo.save(p);
            return ReserveResult.OK;
        }).orElse(ReserveResult.NOT_FOUND);
    }

    public enum ReserveResult { OK, NOT_FOUND, NOT_ENOUGH_STOCK }
}
