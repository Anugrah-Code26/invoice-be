package com.invoice.backend.infrastructure.invoice.repository;

import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.Invoice.Status;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.user.id = :userId")
    Optional<InvoiceResponseDTO> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.user.id = :userId")
    Optional<Invoice> findByIdAndUserIdOpt(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status")
    List<Invoice> findByStatus(@Param("status") Status status);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId")
    List<Invoice> findByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.status = :status")
    List<Invoice> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Status status);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.client.id = :clientId")
    List<Invoice> findByUserIdAndClientId(@Param("userId") Long userId, @Param("clientId") Long clientId);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate <= :date AND i.status = :status")
    List<Invoice> findByDueDateLessThanEqualAndStatus(@Param("date") LocalDate date, @Param("status") Status status);

    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.isRecurring = true AND i.nextRecurringDate <= :date")
    List<Invoice> findByIsRecurringTrueAndNextRecurringDateLessThanEqual(@Param("date") LocalDate date);
}
