package com.easyshop.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;
    Long productId;
    String name;
    BigDecimal price;
    Integer quantity;
}
