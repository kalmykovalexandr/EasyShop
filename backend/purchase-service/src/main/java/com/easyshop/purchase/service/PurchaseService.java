package com.easyshop.purchase.service;

import com.easyshop.purchase.client.ProductClient;
import com.easyshop.purchase.domain.Purchase;
import com.easyshop.purchase.domain.PurchaseItem;
import com.easyshop.purchase.domain.PurchaseItemRepository;
import com.easyshop.purchase.domain.PurchaseRepository;
import com.easyshop.purchase.web.dto.CheckoutDto;
import com.easyshop.purchase.web.dto.PurchaseItemDto;
import com.easyshop.purchase.web.dto.PurchaseResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PurchaseService {
    private final PurchaseRepository purchases;
    private final PurchaseItemRepository items;
    private final ProductClient productClient;

    public PurchaseService(PurchaseRepository purchases, PurchaseItemRepository items, ProductClient productClient) {
        this.purchases = purchases;
        this.items = items;
        this.productClient = productClient;
    }

    public List<PurchaseResponseDto> list(String email) {
        return purchases.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(p -> new PurchaseResponseDto(
                        p.getId(),
                        p.getTotal(),
                        p.getStatus(),
                        p.getItems().stream()
                                .map(i -> new PurchaseItemDto(
                                        i.getProductId(),
                                        i.getName(),
                                        i.getPrice(),
                                        i.getQuantity()
                                ))
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public PurchaseResponseDto checkout(CheckoutDto dto, String email) {
        List<PurchaseItemDto> det = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutDto.Item item : dto.items()) {
            try {
                ProductClient.ProductInfo productInfo = productClient.getProduct(item.productId());

                if (productInfo.stock() < item.quantity()) {
                    throw new StockNotAvailableException();
                }

                BigDecimal itemTotal = productInfo.price().multiply(BigDecimal.valueOf(item.quantity()));
                total = total.add(itemTotal);

                det.add(new PurchaseItemDto(
                        item.productId(),
                        productInfo.name(),
                        productInfo.price(),
                        item.quantity()
                ));
            } catch (Exception e) {
                if (e instanceof StockNotAvailableException) {
                    throw e;
                }
                throw new ServiceUnavailableException();
            }
        }

        Purchase p = new Purchase();
        p.setUserEmail(email);
        p.setTotal(total);
        p.setStatus("CREATED");
        purchases.save(p);

        for (PurchaseItemDto d : det) {
            PurchaseItem pi = PurchaseItem.builder()
                    .purchase(p)
                    .productId(d.productId())
                    .name(d.name())
                    .price(d.price())
                    .quantity(d.quantity())
                    .build();
            items.save(pi);
        }

        return new PurchaseResponseDto(
                p.getId(),
                p.getTotal(),
                p.getStatus(),
                det
        );
    }

    public static class ProductNotFoundException extends RuntimeException {}
    public static class StockNotAvailableException extends RuntimeException {}
    public static class ServiceUnavailableException extends RuntimeException {}
}
