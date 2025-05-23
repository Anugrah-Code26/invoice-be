package com.invoice.backend.entity.invoice;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "invoices", schema = "remedial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_id_gen")
    @SequenceGenerator(name = "invoice_id_gen", sequenceName = "invoice_id_seq", schema = "remedial", allocationSize = 1)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    public enum PaymentTerms {
        MONTHLY, WEEKLY
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", nullable = false)
    private PaymentTerms paymentTerms;

    public enum Status {
        PENDING, PAID, OVERDUE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column()
    private Status status;

//    @NotNull
//    @Column(name = "total_amount", nullable = false)
//    private Double totalAmount;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

//    public enum RecurringSchedule {
//        MONTH_BEFORE_DUE,
//        TWO_WEEKS_BEFORE_DUE,
//        WEEK_BEFORE_DUE,
//        THREE_DAYS_BEFORE_DUE,
//        DAY_BEFORE_DUE
//    }
//
//    @NotNull
//    @Enumerated(EnumType.STRING)
//    @Column(name = "recurring_schedule", nullable = false)
//    private RecurringSchedule recurringSchedule;

    @Column(name = "next_recurring_date")
    private LocalDate nextRecurringDate;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @JsonManagedReference
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private InvoiceItem invoiceItem;
}