package com.invoice.backend.infrastructure.invoice.dto;

import com.invoice.backend.entity.invoice.Invoice.Status;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceDTO {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String paymentTerms;
    private Status status;
    private Boolean isRecurring = false;
    private String recurringSchedule;
    private LocalDate nextRecurringDate;

    @NotEmpty(message = "Invoice items are required")
    private List<InvoiceItemDTO> items;
}