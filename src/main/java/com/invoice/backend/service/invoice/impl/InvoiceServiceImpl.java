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
import com.invoice.backend.infrastructure.invoice.dto.InvoiceResponseDTO;
import com.invoice.backend.infrastructure.invoice.repository.InvoiceRepository;
import com.invoice.backend.infrastructure.product.repository.ProductRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.invoice.InvoiceService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.service.invoice.PDFGeneratorService;
import com.invoice.backend.service.user.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final PDFGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    @Override
    @Transactional
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
        invoice.setIsRecurring(req.getIsRecurring());
//        invoice.setRecurringSchedule(req.getRecurringSchedule());
        invoice.setNextRecurringDate(calculateNextRecurringDate(LocalDate.now(), Invoice.PaymentTerms.valueOf(req.getPaymentTerms())));

        Set<InvoiceItem> items = req.getItems().stream()
                .map(itemReq -> createInvoiceItem(itemReq, invoice))
                .collect(Collectors.toSet());

        invoice.setItems(items);

        double totalAmount = items.stream()
                .mapToDouble(InvoiceItem::getTotalPrice)
                .sum();
        invoice.setTotalAmount(totalAmount);

        return invoiceRepository.save(invoice);
    }

    private InvoiceItem createInvoiceItem(InvoiceItemDTO req, Invoice invoice) throws DataNotFoundException {
        InvoiceItem item = new InvoiceItem();

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        item.setInvoice(invoice);
        item.setProduct(product);
        item.setDescription(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(req.getQuantity());
        item.setTotalPrice(item.getUnitPrice() * item.getQuantity());

        return item;
    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return invoiceRepository.findAll().stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByStatus(Invoice.Status status) {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return invoiceRepository.findByStatus(status).stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InvoiceResponseDTO> getInvoiceById(Long id) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));

        return Optional.of(InvoiceResponseDTO.fromEntity(invoice));
    }

    @Override
    public Invoice updateInvoiceStatus(Long id, Invoice.Status status) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));

        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void sendInvoiceByEmail(Long invoiceId) throws MessagingException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));

        String subject = "Invoice #" + invoice.getInvoiceNumber();
        String body = "Dear " + invoice.getClient().getName() + ",<br><br>"
                + "Please find your invoice attached.<br><br>Thank you.";

        byte[] pdf = pdfGeneratorService.generateInvoicePdf(invoice);

        emailService.sendInvoiceEmail(invoice.getClient().getEmail(), subject, body, pdf);
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

            Set<InvoiceItem> invoiceItems = template.getItems().stream()
                    .map(original -> copyInvoiceItem(original, newInvoice))
                    .collect(Collectors.toSet());

            newInvoice.setItems(invoiceItems);
            newInvoice.setTotalAmount(template.getTotalAmount());
            newInvoice.setIsRecurring(true);
//            newInvoice.setRecurringSchedule(template.getRecurringSchedule());
            newInvoice.setNextRecurringDate(
                    calculateNextRecurringDate(LocalDate.now(), template.getPaymentTerms())
            );

            invoiceRepository.save(newInvoice);

            template.setNextRecurringDate(newInvoice.getNextRecurringDate());
            invoiceRepository.save(template);
        }
    }

    private InvoiceItem copyInvoiceItem(InvoiceItem original, Invoice newInvoice) {
        InvoiceItem copy = new InvoiceItem();
        copy.setInvoice(newInvoice);
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
