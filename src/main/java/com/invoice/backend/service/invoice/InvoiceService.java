package com.invoice.backend.service.invoice;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(InvoiceDTO invoiceDTO) throws DataNotFoundException;
    List<Invoice> getAllInvoices();
    List<Invoice> getInvoicesByStatus(Invoice.Status status);
    Invoice getInvoiceById(Long id) throws DataNotFoundException;
    Invoice updateInvoiceStatus(Long id, Invoice.Status status) throws DataNotFoundException;
    void processRecurringInvoices();
}
