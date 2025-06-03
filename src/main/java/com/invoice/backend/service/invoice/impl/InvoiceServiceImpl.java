package com.invoice.backend.service.invoice.impl;

import com.invoice.backend.common.exceptions.UnauthorizedException;
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
import com.invoice.backend.service.invoice.specification.InvoiceSpecification;
import com.invoice.backend.service.user.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

        String invoiceCode = InvoiceCodeGenerator.generateInvoiceCode(userId, client.getId(), LocalDate.now());
        invoice.setInvoiceNumber(invoiceCode);

        invoice.setIssueDate(LocalDate.now());

        Invoice.PaymentTerms paymentTerms;
        try {
            paymentTerms = Invoice.PaymentTerms.valueOf(req.getPaymentTerms());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid payment term: " + req.getPaymentTerms());
        }

        invoice.setPaymentTerms(paymentTerms);
        invoice.setDueDate(calculateDueDate(LocalDate.now(), paymentTerms));
        invoice.setStatus(Invoice.Status.PENDING);
//        invoice.setRecurringSchedule(req.getRecurringSchedule());

        if (paymentTerms == Invoice.PaymentTerms.TODAY) {
            invoice.setIsRecurring(false);
            invoice.setNextRecurringDate(null);
        } else {
            invoice.setIsRecurring(true);
            invoice.setNextRecurringDate(calculateNextRecurringDate(LocalDate.now(), paymentTerms));
        }

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
        Long userId = Claims.getUserIdFromJwt();

        InvoiceItem item = new InvoiceItem();

        Product product = productRepository.findByIdAndUserId(req.getProductId(), userId);
        if (product == null) {
            throw new UnauthorizedException("Unauthorized Product or Product not found!");
        }

        item.setInvoice(invoice);
        item.setProduct(product);
        item.setDescription(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(req.getQuantity());
        item.setTotalPrice(item.getUnitPrice() * item.getQuantity());

        return item;
    }

    public List<InvoiceResponseDTO> searchInvoices(String invoiceNumber, String clientName, String date, String status) {
        Long userId = Claims.getUserIdFromJwt();

        Specification<Invoice> spec = Specification
                .where(InvoiceSpecification.hasUserId(userId))
                .and(InvoiceSpecification.hasInvoiceNumber(invoiceNumber))
                .and(InvoiceSpecification.hasClientName(clientName))
                .and(InvoiceSpecification.hasDate(date))
                .and(InvoiceSpecification.hasStatus(status));

        List<Invoice> invoices = invoiceRepository.findAll(spec);
        return invoices.stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {
        String role = Claims.getRoleFromJwt();

        if (!"SUPER_ADMIN".equals(role)){
            throw new UnauthorizedException("Unauthorized!");
        }

        return invoiceRepository.findAll().stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByStatus(Invoice.Status status) {
        Long userId = Claims.getUserIdFromJwt();

        List<Invoice> checkInvoice = invoiceRepository.findByUserIdAndStatus(userId, status);
        if (checkInvoice == null) {
            throw new UnauthorizedException("Unauthorized Invoice or Invoice not found!");
        }

        return invoiceRepository.findByStatus(status).stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InvoiceResponseDTO> getInvoiceById(Long id) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        Optional<InvoiceResponseDTO> invoice = invoiceRepository.findByIdAndUserId(id, userId);
        if (invoice.isEmpty()) {
            throw new UnauthorizedException("Unauthorized Invoice or Invoice not found!");
        }

        return invoice;
    }

    @Override
    public Invoice updateInvoiceStatus(Long id, Invoice.Status status) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        Optional<InvoiceResponseDTO> checkInvoice = invoiceRepository.findByIdAndUserId(id, userId);
        if (checkInvoice.isEmpty()) {
            throw new UnauthorizedException("Unauthorized Invoice!");
        }

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));

        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void updateStatusToOverDue() {
        List<Invoice> overdueInvoices = invoiceRepository
                .findByDueDateLessThanEqualAndStatus(LocalDate.now(), Invoice.Status.PENDING);

        if (overdueInvoices != null && !overdueInvoices.isEmpty()) {
            for (Invoice invoice : overdueInvoices) {
                invoice.setStatus(Invoice.Status.OVERDUE);
            }
            invoiceRepository.saveAll(overdueInvoices);
            System.out.println("Updated " + overdueInvoices.size() + " invoices to OVERDUE.");
        } else {
            System.out.println("No overdue invoices found.");
        }
    }

    @Override
    public void sendInvoiceByEmail(Long invoiceId) throws MessagingException {
        Long userId = Claims.getUserIdFromJwt();

        Optional<InvoiceResponseDTO> checkInvoice = invoiceRepository.findByIdAndUserId(invoiceId, userId);
        if (checkInvoice.isEmpty()) {
            throw new UnauthorizedException("Unauthorized Invoice!");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found"));

        String subject = "Invoice #" + invoice.getInvoiceNumber();
        String body = "<p>Dear " + invoice.getClient().getName() + ",</p>" +
                "<p>Thank you for your business. Please find attached the invoice for your recent transaction.</p>" +
                "<p><strong>Invoice Number:</strong> " + invoice.getInvoiceNumber() + "<br>" +
                "<strong>Issue Date:</strong> " + invoice.getIssueDate() + "<br>" +
                "<strong>Due Date:</strong> " + invoice.getDueDate() + "</p>" +
                "<p>If you have any questions, feel free to contact us.</p>" +
                "<p>Best regards,<br>MyInvoice</p>";

        byte[] pdfBytes;
        try {
            pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }

        String filename = "Invoice-" + invoice.getInvoiceNumber() + ".pdf";

        emailService.sendInvoiceEmail(
                invoice.getClient().getEmail(),
                subject,
                body,
                pdfBytes,
                filename
        );
    }

    @Override
    public void processRecurringInvoices() {
        List<Invoice> recurringInvoices = invoiceRepository
                .findByIsRecurringTrueAndNextRecurringDateLessThanEqual(LocalDate.now());

        log.info("Found {} recurring invoices to process", recurringInvoices.size());

        for (Invoice template : recurringInvoices) {
            try {
                Invoice newInvoice = new Invoice();
                newInvoice.setUser(template.getUser());
                newInvoice.setClient(template.getClient());
                newInvoice.setInvoiceNumber(generateNewInvoiceNumber(
                        template.getUser().getId(),
                        template.getClient().getId()
                ));
                newInvoice.setIssueDate(LocalDate.now());
                newInvoice.setPaymentTerms(template.getPaymentTerms());
                newInvoice.setDueDate(calculateDueDate(LocalDate.now(), template.getPaymentTerms()));
                newInvoice.setStatus(Invoice.Status.PENDING);
                newInvoice.setIsRecurring(true);
//                newInvoice.setRecurringSchedule(template.getRecurringSchedule());
                newInvoice.setNextRecurringDate(
                        calculateNextRecurringDate(LocalDate.now(), template.getPaymentTerms())
                );

                Set<InvoiceItem> invoiceItems = template.getItems() == null ?
                        new HashSet<>() :
                        template.getItems().stream()
                                .map(original -> copyInvoiceItem(original, newInvoice))
                                .collect(Collectors.toSet());

                newInvoice.setItems(invoiceItems);
                newInvoice.setTotalAmount(template.getTotalAmount());

                invoiceRepository.save(newInvoice);

                template.setIsRecurring(false);
                invoiceRepository.save(template);
                log.info("Processed recurring invoice template ID: {}", template.getId());
            } catch (Exception e) {
                log.error("Failed to process recurring invoice template ID: {}", template.getId(), e);
            }
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

    private String generateNewInvoiceNumber(Long userId, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new DataNotFoundException("Client not found"));

        String invoiceCode = InvoiceCodeGenerator.generateInvoiceCode(userId, client.getId(), LocalDate.now());
        return invoiceCode + "-R";
    }

    private LocalDate calculateDueDate(LocalDate issueDate, Invoice.PaymentTerms paymentTerms) {
        if (paymentTerms == Invoice.PaymentTerms.MONTHLY) {
            return issueDate.plusMonths(1);
        } else if (paymentTerms == Invoice.PaymentTerms.WEEKLY) {
            return issueDate.plusWeeks(1);
        } else if (paymentTerms == Invoice.PaymentTerms.TODAY) {
            return issueDate.plusDays(1);
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
