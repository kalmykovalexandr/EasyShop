package com.easyshop.purchase.web.dto;

import java.math.BigDecimal;

/**
 * Data transfer object representing an item inside a purchase.
 */
public record PurchaseItemDto(Long productId, String name, BigDecimal price, Integer quantity) {
}
