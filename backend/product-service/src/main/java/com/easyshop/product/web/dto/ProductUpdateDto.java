package com.easyshop.product.web.dto;

import jakarta.validation.constraints.*;

import java.math.*;

public record ProductUpdateDto(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @NotNull @Min(0) Integer stock
) {
}
