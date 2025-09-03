package com.easyshop.purchase.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.*;

@Entity
@Table(name = "purchase_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    Purchase purchase;
    Long productId;
    String name;
    BigDecimal price;
    Integer quantity;
}
