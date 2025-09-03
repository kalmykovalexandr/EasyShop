package com.easyshop.purchase.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "purchase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String userEmail;
    @Column(nullable = false)
    BigDecimal total;
    @Column(nullable = false)
    String status;
    @Column(nullable = false)
    @Builder.Default
    Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    List<PurchaseItem> items = new ArrayList<>();
}
