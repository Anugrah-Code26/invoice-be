package com.invoice.backend.entity.invoice;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.invoice.backend.entity.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Entity
@Table(name = "invoice_items", schema = "remedial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_item_id_gen")
    @SequenceGenerator(name = "invoice_item_id_gen", sequenceName = "invoice_item_id_seq", schema = "remedial", allocationSize = 1)
    private Long id;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column
    private String description;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @NotNull
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}