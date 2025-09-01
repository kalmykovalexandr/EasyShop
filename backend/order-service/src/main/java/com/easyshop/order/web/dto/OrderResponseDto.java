package com.easyshop.order.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data transfer object representing a user order.
 */
public record OrderResponseDto(Long id, BigDecimal total, String status, List<OrderItemDto> items) {
}
