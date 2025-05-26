package com.invoice.backend.infrastructure.invoice.dto;

import com.invoice.backend.entity.invoice.InvoiceItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InvoiceItemResponseDTO {
    private Long id;
    private Long productId;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;

    public static InvoiceItemResponseDTO fromEntity(InvoiceItem item) {
        InvoiceItemResponseDTO dto = new InvoiceItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
}