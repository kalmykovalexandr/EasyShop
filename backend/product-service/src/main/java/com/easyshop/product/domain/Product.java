package com.easyshop.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String name;
    String description;
    @Column(nullable = false)
    BigDecimal price;
    @Column(nullable = false)
    Integer stock;
}
