package com.invoice.backend.infrastructure.invoice.controller;

import com.invoice.backend.common.responses.ApiResponse;
import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceResponseDTO;
import com.invoice.backend.infrastructure.invoice.repository.InvoiceRepository;
import com.invoice.backend.service.invoice.InvoiceService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.service.invoice.PDFGeneratorService;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PDFGeneratorService pdfGeneratorService;
    private final InvoiceRepository invoiceRepository;

    @PostMapping
    public ResponseEntity<?> createInvoice(
            @Valid @RequestBody InvoiceDTO invoiceDTO) throws DataNotFoundException {
        return ApiResponse.success(HttpStatus.OK.value(), "Create invoice success!", invoiceService.createInvoice(invoiceDTO));
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendInvoiceToClient(@PathVariable Long id) throws MessagingException, DocumentException {
        invoiceService.sendInvoiceByEmail(id);
        return ApiResponse.success("Invoice sent!");
    }

    @GetMapping
    public ResponseEntity<?> getInvoices(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(HttpStatus.OK.value(), "Get invoices success!", invoiceService.searchInvoices(invoiceNumber, clientName, date, status));
    }

//    @GetMapping
//    public ResponseEntity<?> getAllInvoices() {
//        return ApiResponse.success(HttpStatus.OK.value(), "Get all invoices success!", invoiceService.getAllInvoices());
//    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getInvoicesByStatus(
            @PathVariable String status) {
        return ApiResponse.success(HttpStatus.OK.value(), "Get invoice by status success!", invoiceService.getInvoicesByStatus(Invoice.Status.valueOf(status.toUpperCase())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInvoiceById(
            @PathVariable Long id) throws DataNotFoundException {
        return ApiResponse.success(HttpStatus.OK.value(), "Get invoice by id success!", invoiceService.getInvoiceById(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws DocumentException {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));
        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateInvoiceStatus(
            @PathVariable Long id,
            @PathVariable String status) throws DataNotFoundException {
        return ApiResponse.success(HttpStatus.OK.value(), "Update invoice status success!", invoiceService.updateInvoiceStatus(
                id,
                Invoice.Status.valueOf(status.toUpperCase())
        ));
    }

    @PostMapping("/process-recurring")
    public ResponseEntity<?> processRecurringInvoices() {
        invoiceService.processRecurringInvoices();
        return ApiResponse.success("Recurring invoice success!");
    }
}
