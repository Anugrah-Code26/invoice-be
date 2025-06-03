package com.invoice.backend.service.invoice.specification;

import com.invoice.backend.entity.invoice.Invoice;
import org.springframework.data.jpa.domain.Specification;

public class InvoiceSpecification {

    public static Specification<Invoice> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Invoice> hasInvoiceNumber(String invoiceNumber) {
        return (root, query, cb) ->
                invoiceNumber == null ? null : cb.like(cb.lower(root.get("invoiceNumber")), "%" + invoiceNumber.toLowerCase() + "%");
    }

    public static Specification<Invoice> hasClientName(String clientName) {
        return (root, query, cb) ->
                clientName == null ? null : cb.like(cb.lower(root.join("client").get("name")), "%" + clientName.toLowerCase() + "%");
    }

    public static Specification<Invoice> hasDate(String date) {
        return (root, query, cb) ->
                date == null ? null : cb.like(root.get("dueDate").as(String.class), "%" + date + "%");
    }

    public static Specification<Invoice> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty()) {
                return null;
            }
            try {
                Invoice.Status invoiceStatus = Invoice.Status.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), invoiceStatus);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }
}
