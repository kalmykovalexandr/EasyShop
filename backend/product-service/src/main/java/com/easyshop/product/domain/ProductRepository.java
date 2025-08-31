package com.easyshop.product.domain;

import org.springframework.data.jpa.repository.*;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
