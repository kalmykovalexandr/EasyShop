package com.easyshop.product.service;

import com.easyshop.product.domain.Product;
import com.easyshop.product.domain.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repo;

    @Test
    void reserveProductSuccessfully() {
        Product p = repo.save(Product.builder()
                .name("Test")
                .description("desc")
                .price(BigDecimal.TEN)
                .stock(5)
                .build());

        var result = service.reserve(p.getId(), 3);
        assertThat(result).isEqualTo(ProductService.ReserveResult.OK);
        assertThat(repo.findById(p.getId()).orElseThrow().getStock()).isEqualTo(2);
    }

    @Test
    void reserveFailsWhenNotEnoughStock() {
        Product p = repo.save(Product.builder()
                .name("Test")
                .description("desc")
                .price(BigDecimal.TEN)
                .stock(2)
                .build());

        var result = service.reserve(p.getId(), 3);
        assertThat(result).isEqualTo(ProductService.ReserveResult.NOT_ENOUGH_STOCK);
        assertThat(repo.findById(p.getId()).orElseThrow().getStock()).isEqualTo(2);
    }

    @Test
    void reserveFailsWhenProductNotFound() {
        var result = service.reserve(999L, 1);
        assertThat(result).isEqualTo(ProductService.ReserveResult.NOT_FOUND);
    }
}

