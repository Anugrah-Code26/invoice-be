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
    private String clientName;
    private String clientEmail;
    private String clientPhoneNumber;
    private String clientAddress;

    private Long id;
    private String invoiceNumber;
    private Double totalAmount;
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
        dto.setClientName(invoice.getClient().getName());
        dto.setClientEmail(invoice.getClient().getEmail());
        dto.setClientPhoneNumber(invoice.getClient().getPhoneNumber());
        dto.setClientAddress(invoice.getClient().getAddress());

        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setTotalAmount(invoice.getTotalAmount());
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