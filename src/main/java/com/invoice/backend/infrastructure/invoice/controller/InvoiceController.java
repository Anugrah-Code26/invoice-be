package com.invoice.backend.infrastructure.invoice.controller;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.service.invoice.InvoiceService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(
            @Valid @RequestBody InvoiceDTO invoiceDTO,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(invoiceDTO, user));
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(user));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStatus(user, Invoice.Status.valueOf(status.toUpperCase())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id, user));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @PathVariable Long id,
            @PathVariable String status,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(
                id,
                Invoice.Status.valueOf(status.toUpperCase()),
                user));
    }

    @PostMapping("/process-recurring")
    public ResponseEntity<Void> processRecurringInvoices() {
        invoiceService.processRecurringInvoices();
        return ResponseEntity.ok().build();
    }
}
