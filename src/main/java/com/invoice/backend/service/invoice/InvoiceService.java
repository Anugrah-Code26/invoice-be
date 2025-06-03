package com.invoice.backend.service.invoice;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceResponseDTO;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    Invoice createInvoice(InvoiceDTO invoiceDTO) throws DataNotFoundException;
    List<InvoiceResponseDTO> searchInvoices(String invoiceNumber, String clientName, String date, String status);
    List<InvoiceResponseDTO> getAllInvoices();
    List<InvoiceResponseDTO> getInvoicesByStatus(Invoice.Status status);
    Optional<InvoiceResponseDTO> getInvoiceById(Long id) throws DataNotFoundException;
    Invoice updateInvoiceStatus(Long id, Invoice.Status status) throws DataNotFoundException;
    void updateStatusToOverDue();
    void sendInvoiceByEmail(Long invoiceId) throws MessagingException, DocumentException;
    void processRecurringInvoices();
}
