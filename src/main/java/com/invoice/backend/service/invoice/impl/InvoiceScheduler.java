package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.service.invoice.InvoiceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    @PostConstruct
    public void runOnStartup() {
        try {
            log.info("Running recurring invoice job on startup at: {}", LocalDateTime.now());
            invoiceService.processRecurringInvoices();

            log.info("Running overdue invoice job on startup at: {}", LocalDateTime.now());
            invoiceService.updateStatusToOverDue();
        } catch (Exception e) {
            log.error("❌ Error during InvoiceScheduler startup", e);
        }
    }


    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Jakarta") // Every day at 8 AM Jakarta time
    public void updateOverdueInvoicesJob() {
        System.out.println("Updating overdue invoice statuses at: " + LocalDateTime.now());
        invoiceService.updateStatusToOverDue();
    }


    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Jakarta")
    public void processRecurringInvoicesJob() {
        System.out.println("Running recurring invoice check at: " + LocalDateTime.now());
        invoiceService.processRecurringInvoices();
    }
}
