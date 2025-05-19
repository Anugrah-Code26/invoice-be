package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.InvoiceItem;
import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceItemDTO;
import com.invoice.backend.infrastructure.invoice.repository.InvoiceRepository;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.service.invoice.InvoiceService;
import com.invoice.backend.service.product.ProductService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ClientService clientService;
    private final ProductService productService;

    @Override
    public Invoice createInvoice(InvoiceDTO invoiceDTO, User user) throws DataNotFoundException {
        Client client = clientService.getClientById(invoiceDTO.getClientId(), user);

        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setClient(client);
        invoice.setInvoiceNumber(invoiceDTO.getInvoiceNumber());
        invoice.setIssueDate(invoiceDTO.getIssueDate());
        invoice.setDueDate(invoiceDTO.getDueDate());
        invoice.setPaymentTerms(invoiceDTO.getPaymentTerms());
        invoice.setStatus(invoiceDTO.getStatus() != null ? invoiceDTO.getStatus() : Invoice.Status.PENDING);
        invoice.setIsRecurring(invoiceDTO.getIsRecurring());
        invoice.setRecurringSchedule(invoiceDTO.getRecurringSchedule());
        invoice.setNextRecurringDate(invoiceDTO.getNextRecurringDate());

        List<InvoiceItem> items = invoiceDTO.getItems().stream()
                .map(itemDTO -> createInvoiceItem(itemDTO, user))
                .collect(Collectors.toList());

        invoice.setItems(items);

        double totalAmount = items.stream()
                .mapToDouble(InvoiceItem::getTotalPrice)
                .sum();
        invoice.setTotalAmount(totalAmount);

        return invoiceRepository.save(invoice);
    }

    private InvoiceItem createInvoiceItem(InvoiceItemDTO itemDTO, User user) throws DataNotFoundException {
        InvoiceItem item = new InvoiceItem();

        if (itemDTO.getProductId() != null) {
            Product product = productService.getProductById(itemDTO.getProductId(), user);
            item.setProduct(product);
            item.setDescription(product.getName());
            item.setUnitPrice(product.getPrice());
        } else {
            item.setDescription(itemDTO.getDescription());
            item.setUnitPrice(itemDTO.getUnitPrice());
        }

        item.setQuantity(itemDTO.getQuantity());
        item.setTotalPrice(item.getUnitPrice() * item.getQuantity());

        return item;
    }

    @Override
    public List<Invoice> getAllInvoices(User user) {
        return invoiceRepository.findByUserId(user.getId());
    }

    @Override
    public List<Invoice> getInvoicesByStatus(User user, Invoice.Status status) {
        return invoiceRepository.findByUserIdAndStatus(user.getId(), status);
    }

    @Override
    public Invoice getInvoiceById(Long id, User user) throws DataNotFoundException {
        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));
    }

    @Override
    public Invoice updateInvoiceStatus(Long id, Invoice.Status status, User user) throws DataNotFoundException {
        Invoice invoice = getInvoiceById(id, user);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void processRecurringInvoices() {
        List<Invoice> recurringInvoices = invoiceRepository.findByIsRecurringTrueAndNextRecurringDateLessThanEqual(LocalDate.now());

        for (Invoice template : recurringInvoices) {
            Invoice newInvoice = new Invoice();
            newInvoice.setUser(template.getUser());
            newInvoice.setClient(template.getClient());
            newInvoice.setInvoiceNumber(generateNewInvoiceNumber(template.getInvoiceNumber()));
            newInvoice.setIssueDate(LocalDate.now());
            newInvoice.setDueDate(calculateDueDate(LocalDate.now(), template.getPaymentTerms()));
            newInvoice.setPaymentTerms(template.getPaymentTerms());
            newInvoice.setStatus(Invoice.Status.PENDING);
            newInvoice.setItems(template.getItems().stream()
                    .map(this::copyInvoiceItem)
                    .collect(Collectors.toList()));
            newInvoice.setTotalAmount(template.getTotalAmount());
            newInvoice.setIsRecurring(true);
            newInvoice.setRecurringSchedule(template.getRecurringSchedule());
            newInvoice.setNextRecurringDate(calculateNextRecurringDate(LocalDate.now(), template.getRecurringSchedule()));

            invoiceRepository.save(newInvoice);

            template.setNextRecurringDate(newInvoice.getNextRecurringDate());
            invoiceRepository.save(template);
        }
    }

    private String generateNewInvoiceNumber(String originalNumber) {
        // Implementation for generating new invoice numbers
        return originalNumber + "-R";
    }

    private LocalDate calculateDueDate(LocalDate issueDate, String paymentTerms) {
        // Implementation based on payment terms
        return issueDate.plusDays(30); // Default 30 days
    }

    private LocalDate calculateNextRecurringDate(LocalDate currentDate, String schedule) {
        if ("MONTHLY".equals(schedule)) {
            return currentDate.plusMonths(1);
        } else if ("WEEKLY".equals(schedule)) {
            return currentDate.plusWeeks(1);
        }
        return currentDate.plusMonths(1); // Default monthly
    }

    private InvoiceItem copyInvoiceItem(InvoiceItem original) {
        InvoiceItem copy = new InvoiceItem();
        copy.setProduct(original.getProduct());
        copy.setDescription(original.getDescription());
        copy.setQuantity(original.getQuantity());
        copy.setUnitPrice(original.getUnitPrice());
        copy.setTotalPrice(original.getTotalPrice());
        return copy;
    }
}
