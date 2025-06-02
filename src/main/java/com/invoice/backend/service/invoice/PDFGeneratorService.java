package com.invoice.backend.service.invoice;

import com.invoice.backend.entity.invoice.Invoice;
import com.itextpdf.text.DocumentException;

public interface PDFGeneratorService {
    public byte[] generateInvoicePdf(Invoice invoice) throws DocumentException;
}
