package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.service.invoice.InvoiceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    public InvoiceScheduler(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void processRecurringInvoicesJob() {
        System.out.println("🕒 Running recurring invoice check at: " + LocalDateTime.now());
        invoiceService.processRecurringInvoices();
    }
}