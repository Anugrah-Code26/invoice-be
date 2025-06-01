package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.InvoiceItem;
import com.invoice.backend.service.invoice.PDFGeneratorService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PDFGeneratorServiceImpl implements PDFGeneratorService {

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("Invoice #" + invoice.getInvoiceNumber());
            content.endText();

            content.setFont(PDType1Font.HELVETICA, 12);
            int y = 720;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Client: " + invoice.getClient().getName());
            content.newLineAtOffset(0, -15);
            content.showText("Email: " + invoice.getClient().getEmail());
            content.newLineAtOffset(0, -15);
            content.showText("Issue Date: " + invoice.getIssueDate());
            content.newLineAtOffset(0, -15);
            content.showText("Due Date: " + invoice.getDueDate());
            content.endText();

            y -= 80;

            for (InvoiceItem item : invoice.getItems()) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(item.getProduct().getName() + " - $" + item.getUnitPrice() + " x " + item.getQuantity() + " = $" + item.getTotalPrice());
                content.endText();
                y -= 20;
            }

            content.beginText();
            content.newLineAtOffset(50, y - 10);
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.showText("Total: $" + invoice.getTotalAmount());
            content.endText();

            content.close();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
