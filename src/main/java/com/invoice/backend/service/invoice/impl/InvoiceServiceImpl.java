package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.common.utils.InvoiceCodeGenerator;
import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.invoice.Invoice;
import com.invoice.backend.entity.invoice.InvoiceItem;
import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.auth.Claims;
import com.invoice.backend.infrastructure.client.repository.ClientRepository;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceDTO;
import com.invoice.backend.infrastructure.invoice.dto.InvoiceItemDTO;
import com.invoice.backend.infrastructure.invoice.repository.InvoiceRepository;
import com.invoice.backend.infrastructure.product.repository.ProductRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.service.invoice.InvoiceService;
import com.invoice.backend.service.product.ProductService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ClientService clientService;
    private final ProductService productService;

    @Override
    public Invoice createInvoice(InvoiceDTO req) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new DataNotFoundException("Client not found"));

        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setClient(client);

        String invoiceCode = InvoiceCodeGenerator.generateInvoiceCode(userId, LocalDate.now());
        invoice.setInvoiceNumber(invoiceCode);

        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(calculateDueDate(LocalDate.now(), Invoice.PaymentTerms.valueOf(req.getPaymentTerms())));
        invoice.setPaymentTerms(Invoice.PaymentTerms.valueOf(req.getPaymentTerms()));
        invoice.setStatus(req.getStatus() != null ? req.getStatus() : Invoice.Status.PENDING);
        invoice.setIsRecurring(true);
//        invoice.setRecurringSchedule(req.getRecurringSchedule());
        invoice.setNextRecurringDate(calculateNextRecurringDate(LocalDate.now(), Invoice.PaymentTerms.valueOf(req.getPaymentTerms())));

        InvoiceItem item = createInvoiceItem(req.getItem());
        item.setInvoice(invoice);

        invoice.setInvoiceItem(item);

        return invoiceRepository.save(invoice);
    }

    private InvoiceItem createInvoiceItem(InvoiceItemDTO req) throws DataNotFoundException {
        InvoiceItem item = new InvoiceItem();

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        item.setProduct(product);
        item.setDescription(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(req.getQuantity());
        item.setTotalPrice(item.getUnitPrice() * item.getQuantity());

        return item;
    }

    @Override
    public List<Invoice> getAllInvoices() {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return invoiceRepository.findByUserId(user.getId());
    }

    @Override
    public List<Invoice> getInvoicesByStatus(Invoice.Status status) {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return invoiceRepository.findByUserIdAndStatus(user.getId(), status);
    }

    @Override
    public Invoice getInvoiceById(Long id) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));
    }

    @Override
    public Invoice updateInvoiceStatus(Long id, Invoice.Status status) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Invoice invoice = getInvoiceById(id);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void processRecurringInvoices() {
        List<Invoice> recurringInvoices = invoiceRepository
                .findByIsRecurringTrueAndNextRecurringDateLessThanEqual(LocalDate.now());

        for (Invoice template : recurringInvoices) {
            Invoice newInvoice = new Invoice();
            newInvoice.setUser(template.getUser());
            newInvoice.setClient(template.getClient());
            newInvoice.setInvoiceNumber(generateNewInvoiceNumber(template.getInvoiceNumber()));
            newInvoice.setIssueDate(LocalDate.now());
            newInvoice.setDueDate(calculateDueDate(LocalDate.now(), template.getPaymentTerms()));
            newInvoice.setPaymentTerms(template.getPaymentTerms());
            newInvoice.setStatus(Invoice.Status.PENDING);
            newInvoice.setIsRecurring(true);
//            newInvoice.setRecurringSchedule(template.getRecurringSchedule());
            newInvoice.setNextRecurringDate(
                    calculateNextRecurringDate(LocalDate.now(), template.getPaymentTerms())
            );

            // Copy the item details
            InvoiceItem oldItem = template.getInvoiceItem();
            InvoiceItem newItem = new InvoiceItem();
            newItem.setProduct(oldItem.getProduct());
            newItem.setDescription(oldItem.getDescription());
            newItem.setUnitPrice(oldItem.getUnitPrice());
            newItem.setQuantity(oldItem.getQuantity());
            newItem.setTotalPrice(oldItem.getUnitPrice() * oldItem.getQuantity());
            newItem.setInvoice(newInvoice);

            newInvoice.setInvoiceItem(newItem);

            // Save the new invoice (cascades to item)
            invoiceRepository.save(newInvoice);

            // Update template's next recurring date
            template.setNextRecurringDate(newInvoice.getNextRecurringDate());
            invoiceRepository.save(template);
        }
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

    private String generateNewInvoiceNumber(String originalNumber) {
        // Implementation for generating new invoice numbers
        return originalNumber + "-R";
    }

    private LocalDate calculateDueDate(LocalDate issueDate, Invoice.PaymentTerms paymentTerms) {
        if (paymentTerms == Invoice.PaymentTerms.MONTHLY) {
            return issueDate.plusMonths(1);
        } else if (paymentTerms == Invoice.PaymentTerms.WEEKLY) {
            return issueDate.plusWeeks(1);
        }
        return issueDate.plusMonths(1);
    }


    private LocalDate calculateNextRecurringDate(
            LocalDate issueDate,
            Invoice.PaymentTerms paymentTerms
//            Invoice.RecurringSchedule recurringSchedule
    ) {
//        if (paymentTerms == Invoice.PaymentTerms.MONTHLY) {
//            if (recurringSchedule == Invoice.RecurringSchedule.MONTH_BEFORE_DUE) {
//                return issueDate.plusMonths(1);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.TWO_WEEKS_BEFORE_DUE) {
//                return issueDate.plusMonths(1).plusWeeks(2);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.WEEK_BEFORE_DUE) {
//                return issueDate.plusWeeks(3);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.THREE_DAYS_BEFORE_DUE) {
//                return issueDate.plusMonths(1).minusDays(3);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.DAY_BEFORE_DUE) {
//                return issueDate.plusMonths(1).minusDays(1);
//            }
//            return issueDate.plusMonths(1);
//        } else if (paymentTerms == Invoice.PaymentTerms.WEEKLY) {
//            if (recurringSchedule == Invoice.RecurringSchedule.WEEK_BEFORE_DUE) {
//                return issueDate.plusWeeks(1);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.THREE_DAYS_BEFORE_DUE) {
//                return issueDate.plusWeeks(1).minusDays(3);
//            } else if (recurringSchedule == Invoice.RecurringSchedule.DAY_BEFORE_DUE) {
//                return issueDate.plusWeeks(1).minusDays(1);
//            }
//            return issueDate.plusWeeks(1);
//        }
        if (paymentTerms == Invoice.PaymentTerms.MONTHLY) {
            return issueDate.plusMonths(1);
        } else if (paymentTerms == Invoice.PaymentTerms.WEEKLY) {
            return issueDate.plusWeeks(1);
        }
        return issueDate.plusMonths(1);
    }
}
