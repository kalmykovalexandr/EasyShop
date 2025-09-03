package com.easyshop.purchase.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data transfer object representing a user purchase.
 */
public record PurchaseResponseDto(Long id, BigDecimal total, String status, List<PurchaseItemDto> items) {
}
