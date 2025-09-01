package com.easyshop.order.web.dto;

import java.math.BigDecimal;

/**
 * Data transfer object representing an item inside an order.
 */
public record OrderItemDto(Long productId, String name, BigDecimal price, Integer quantity) {
}
