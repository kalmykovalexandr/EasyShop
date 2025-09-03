package com.easyshop.purchase.web.dto;


import java.util.List;

/**
 * Data transfer object for checkout request
 */
public record CheckoutDto(List<Item> items) {

    public record Item(Long productId, Integer quantity) {}
}
