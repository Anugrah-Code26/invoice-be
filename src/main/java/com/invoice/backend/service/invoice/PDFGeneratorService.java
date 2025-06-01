package com.invoice.backend.service.invoice;

import com.invoice.backend.entity.invoice.Invoice;

public interface PDFGeneratorService {
    public byte[] generateInvoicePdf(Invoice invoice);
}
