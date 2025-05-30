package com.invoice.backend.infrastructure.invoice.repository;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.Invoice.Status;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    List<Invoice> findByUserId(Long userId);
    List<Invoice> findByStatus(Status status);
    List<Invoice> findByUserIdAndStatus(Long userId, Status status);
    List<Invoice> findByUserIdAndClientId(Long userId, Long clientId);
    List<Invoice> findByIsRecurringTrueAndNextRecurringDateLessThanEqual(LocalDate date);
}
