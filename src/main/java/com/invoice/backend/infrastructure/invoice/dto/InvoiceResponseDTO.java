package com.invoice.backend.infrastructure.invoice.dto;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.Invoice.Status;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class InvoiceResponseDTO {
    private Long clientId;
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String paymentTerms;
    private Status status;
    private Boolean isRecurring = false;
//    private String recurringSchedule;
    private LocalDate nextRecurringDate;
    private Set<InvoiceItemResponseDTO> items;

    public static InvoiceResponseDTO fromEntity(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setClientId(invoice.getClient().getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentTerms(invoice.getPaymentTerms().name());
        dto.setStatus(invoice.getStatus());
        dto.setIsRecurring(invoice.getIsRecurring());
        dto.setNextRecurringDate(invoice.getNextRecurringDate());
        dto.setItems(invoice.getItems().stream()
                .map(InvoiceItemResponseDTO::fromEntity)
                .collect(Collectors.toSet()));
        return dto;
    }
}