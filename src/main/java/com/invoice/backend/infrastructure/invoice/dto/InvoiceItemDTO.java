package com.invoice.backend.infrastructure.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InvoiceItemDTO {
    private Long productId;
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private Double unitPrice;
}