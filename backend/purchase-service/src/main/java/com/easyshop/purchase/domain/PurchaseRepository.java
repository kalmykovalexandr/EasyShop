package com.easyshop.purchase.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserEmailOrderByCreatedAtDesc(String email);
}
