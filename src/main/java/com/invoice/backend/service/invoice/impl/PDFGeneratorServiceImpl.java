package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.InvoiceItem;
import com.invoice.backend.service.invoice.PDFGeneratorService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
    public byte[] generateInvoicePdf(Invoice invoice) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Company Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Paragraph title = new Paragraph(invoice.getInvoiceNumber(), titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        document.add(title);

        // Invoice Status
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph status = new Paragraph("Status: " + invoice.getStatus().name(), sectionFont);
        status.setSpacingBefore(10f);
        status.setAlignment(Element.ALIGN_RIGHT);
        document.add(status);

        // Company Info
        Paragraph company = new Paragraph(
                invoice.getUser().getName() +
                "\nEmail: " + invoice.getUser().getEmail() +
                "\nPhone: " + invoice.getUser().getPhoneNumber() +
                "\nAddress: " + invoice.getUser().getAddress()
        );
        company.setAlignment(Element.ALIGN_RIGHT);
        document.add(company);

        document.add(new Paragraph("\n"));

        // Client Info
        document.add(new Paragraph("Bill To:", sectionFont));
        document.add(new Paragraph(invoice.getClient().getName()));
        document.add(new Paragraph(invoice.getClient().getEmail()));
        document.add(new Paragraph(invoice.getClient().getAddress()));
        document.add(new Paragraph("\n"));

        // Invoice Details
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{4, 2, 1, 2});

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        addTableHeader(table, headerFont, "Product", "Unit Price", "Qty", "Total");

        for (InvoiceItem item : invoice.getItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell("$" + String.format("%.2f", item.getUnitPrice()));
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("$" + String.format("%.2f", item.getTotalPrice()));
        }

        document.add(table);

        // Total Amount
        Paragraph total = new Paragraph("Total: $" + String.format("%.2f", invoice.getTotalAmount()), sectionFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        // Company Info
        Paragraph myInvoice = new Paragraph("This invoice is sent by MyInvoice\nEmail: myinvoice.info@company.com\nPhone: +1 234 567 890\nAddress: MyInvoice Street");
        company.setAlignment(Element.ALIGN_RIGHT);
        document.add(myInvoice);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}
