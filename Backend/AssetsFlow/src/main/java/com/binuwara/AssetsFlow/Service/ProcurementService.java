package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.InvoiceRequest;
import com.binuwara.AssetsFlow.DTO.InvoiceResponse;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderItemRequest;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderItemResponse;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderRequest;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderResponse;
import com.binuwara.AssetsFlow.DTO.VendorContractRequest;
import com.binuwara.AssetsFlow.DTO.VendorContractResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.AssetCategory;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.Invoice;
import com.binuwara.AssetsFlow.Entity.PurchaseOrder;
import com.binuwara.AssetsFlow.Entity.PurchaseOrderItem;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Entity.VendorContract;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetCategoryRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.InvoiceRepository;
import com.binuwara.AssetsFlow.Repository.PurchaseOrderRepository;
import com.binuwara.AssetsFlow.Repository.VendorContractRepository;
import com.binuwara.AssetsFlow.Repository.VendorRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcurementService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AssetCategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceRepository invoiceRepository;
    private final VendorContractRepository contractRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public ProcurementService(PurchaseOrderRepository purchaseOrderRepository, AssetCategoryRepository categoryRepository, VendorRepository vendorRepository, EmployeeRepository employeeRepository, InvoiceRepository invoiceRepository, VendorContractRepository contractRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.employeeRepository = employeeRepository;
        this.invoiceRepository = invoiceRepository;
        this.contractRepository = contractRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> listOrders(String status) {
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc().stream()
                .filter(order -> !StringUtils.hasText(status) || order.getStatus().name().equalsIgnoreCase(status.trim()))
                .map(this::orderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getOrder(UUID id) {
        return orderResponse(purchaseOrderRepository.findWithDetailsById(id).orElseThrow(() -> DomainSupport.notFound("Purchase order")));
    }

    @Transactional
    public PurchaseOrderResponse createOrder(PurchaseOrderRequest request, AuthenticatedUser actor) {
        PurchaseOrder order = new PurchaseOrder();
        applyOrder(order, request, true);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "PURCHASE_ORDER", saved.getId(), "CREATED", null);
        return orderResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse updateOrder(UUID id, PurchaseOrderRequest request, AuthenticatedUser actor) {
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Purchase order"));
        applyOrder(order, request, false);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "PURCHASE_ORDER", saved.getId(), "UPDATED", null);
        return orderResponse(saved);
    }

    @Transactional
    public void deleteOrder(UUID id, AuthenticatedUser actor) {
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Purchase order"));
        order.setStatus(com.binuwara.AssetsFlow.Entity.PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(order);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "PURCHASE_ORDER", id, "CANCELLED", null);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices() {
        return invoiceRepository.findAllByOrderByInvoiceDateDesc().stream().map(this::invoiceResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        return invoiceResponse(invoiceRepository.findWithReferencesById(id).orElseThrow(() -> DomainSupport.notFound("Invoice")));
    }

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request, AuthenticatedUser actor) {
        Invoice invoice = new Invoice();
        applyInvoice(invoice, request, true);
        invoice.setRecordedByUser(currentUser(actor));
        Invoice saved = invoiceRepository.save(invoice);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "INVOICE", saved.getId(), "CREATED", null);
        return invoiceResponse(saved);
    }

    @Transactional
    public InvoiceResponse updateInvoice(UUID id, InvoiceRequest request, AuthenticatedUser actor) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Invoice"));
        applyInvoice(invoice, request, false);
        Invoice saved = invoiceRepository.save(invoice);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "INVOICE", saved.getId(), "UPDATED", null);
        return invoiceResponse(saved);
    }

    @Transactional
    public void deleteInvoice(UUID id, AuthenticatedUser actor) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Invoice"));
        invoice.setStatus(com.binuwara.AssetsFlow.Entity.InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "INVOICE", id, "CANCELLED", null);
    }

    @Transactional(readOnly = true)
    public List<VendorContractResponse> listContracts(UUID vendorId) {
        return contractRepository.findAllByOrderByStartDateDesc().stream().filter(contract -> vendorId == null || vendorId.equals(contract.getVendor().getId())).map(this::contractResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendorContractResponse getContract(UUID id) {
        return contractResponse(contractRepository.findWithVendorById(id).orElseThrow(() -> DomainSupport.notFound("Vendor contract")));
    }

    @Transactional
    public VendorContractResponse createContract(VendorContractRequest request, AuthenticatedUser actor) {
        VendorContract contract = new VendorContract();
        applyContract(contract, request, true);
        VendorContract saved = contractRepository.save(contract);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR_CONTRACT", saved.getId(), "CREATED", null);
        return contractResponse(saved);
    }

    @Transactional
    public VendorContractResponse updateContract(UUID id, VendorContractRequest request, AuthenticatedUser actor) {
        VendorContract contract = contractRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor contract"));
        applyContract(contract, request, false);
        VendorContract saved = contractRepository.save(contract);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR_CONTRACT", saved.getId(), "UPDATED", null);
        return contractResponse(saved);
    }

    @Transactional
    public void deleteContract(UUID id, AuthenticatedUser actor) {
        VendorContract contract = contractRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor contract"));
        contract.setStatus(com.binuwara.AssetsFlow.Entity.VendorContractStatus.TERMINATED);
        contractRepository.save(contract);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR_CONTRACT", id, "TERMINATED", null);
    }

    private void applyOrder(PurchaseOrder order, PurchaseOrderRequest request, boolean create) {
        if (create || StringUtils.hasText(request.poNumber())) {
            String number = DomainSupport.text(request.poNumber(), "PO number");
            if ((create && purchaseOrderRepository.existsByPoNumberIgnoreCase(number)) || (!create && purchaseOrderRepository.existsByPoNumberIgnoreCaseAndIdNot(number, order.getId()))) throw DomainSupport.conflict("PO number is already in use.");
            order.setPoNumber(number);
        }
        if (request.vendorId() != null) order.setVendor(vendorRepository.findById(request.vendorId()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        else if (create) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Vendor is required.");
        if (request.requestedByEmployeeId() != null) order.setRequestedByEmployee(employeeRepository.findById(request.requestedByEmployeeId()).orElseThrow(() -> DomainSupport.notFound("Employee")));
        if (request.orderDate() != null) order.setOrderDate(request.orderDate());
        if (request.expectedDate() != null) order.setExpectedDate(request.expectedDate());
        if (request.receivedDate() != null) order.setReceivedDate(request.receivedDate());
        if (request.status() != null) order.setStatus(request.status());
        if (request.currency() != null) order.setCurrency(DomainSupport.currency(request.currency()));
        if (request.notes() != null) order.setNotes(DomainSupport.optionalText(request.notes()));
        if (request.items() != null) replaceItems(order, request.items());
        BigDecimal subtotal = request.subtotal() != null ? nonNegative(request.subtotal(), "Subtotal") : calculateSubtotal(order.getItems());
        BigDecimal tax = request.taxAmount() == null ? BigDecimal.ZERO : nonNegative(request.taxAmount(), "Tax amount");
        BigDecimal total = request.totalAmount() == null ? subtotal.add(tax) : nonNegative(request.totalAmount(), "Total amount");
        order.setSubtotal(money(subtotal));
        order.setTaxAmount(money(tax));
        order.setTotalAmount(money(total));
        if (order.getExpectedDate() != null && order.getOrderDate() != null && order.getExpectedDate().isBefore(order.getOrderDate())) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Expected date cannot be before order date.");
    }

    private void replaceItems(PurchaseOrder order, List<PurchaseOrderItemRequest> requests) {
        order.getItems().clear();
        for (PurchaseOrderItemRequest request : requests) {
            if (!StringUtils.hasText(request.description()) || request.quantity() == null || request.quantity() < 1 || request.unitCost() == null || request.unitCost().signum() < 0) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Each purchase order item needs a description, positive quantity, and non-negative unit cost.");
            int received = request.receivedQuantity() == null ? 0 : request.receivedQuantity();
            if (received < 0 || received > request.quantity()) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Received quantity must be between zero and quantity.");
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(order);
            item.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId()).orElseThrow(() -> DomainSupport.notFound("Category")));
            item.setDescription(request.description().trim());
            item.setQuantity(request.quantity());
            item.setReceivedQuantity(received);
            item.setUnitCost(money(request.unitCost()));
            order.getItems().add(item);
        }
    }

    private void applyInvoice(Invoice invoice, InvoiceRequest request, boolean create) {
        if (create || StringUtils.hasText(request.invoiceNumber())) {
            String number = DomainSupport.text(request.invoiceNumber(), "Invoice number");
            if ((create && invoiceRepository.existsByInvoiceNumberIgnoreCase(number)) || (!create && invoiceRepository.existsByInvoiceNumberIgnoreCaseAndIdNot(number, invoice.getId()))) throw DomainSupport.conflict("Invoice number is already in use.");
            invoice.setInvoiceNumber(number);
        }
        if (request.vendorId() != null) invoice.setVendor(vendorRepository.findById(request.vendorId()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        else if (create) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Vendor is required.");
        if (request.purchaseOrderId() != null) invoice.setPurchaseOrder(purchaseOrderRepository.findById(request.purchaseOrderId()).orElseThrow(() -> DomainSupport.notFound("Purchase order")));
        if (request.invoiceDate() != null) invoice.setInvoiceDate(request.invoiceDate());
        if (request.dueDate() != null) invoice.setDueDate(request.dueDate());
        if (request.paidDate() != null) invoice.setPaidDate(request.paidDate());
        if (request.status() != null) invoice.setStatus(request.status());
        if (request.currency() != null) invoice.setCurrency(DomainSupport.currency(request.currency()));
        if (request.subtotal() != null) invoice.setSubtotal(nonNegative(request.subtotal(), "Subtotal"));
        if (request.taxAmount() != null) invoice.setTaxAmount(nonNegative(request.taxAmount(), "Tax amount"));
        if (request.totalAmount() != null) invoice.setTotalAmount(nonNegative(request.totalAmount(), "Total amount"));
        if (request.notes() != null) invoice.setNotes(DomainSupport.optionalText(request.notes()));
        if (invoice.getDueDate() != null && invoice.getInvoiceDate() != null && invoice.getDueDate().isBefore(invoice.getInvoiceDate())) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Due date cannot be before invoice date.");
    }

    private void applyContract(VendorContract contract, VendorContractRequest request, boolean create) {
        if (request.vendorId() != null) contract.setVendor(vendorRepository.findById(request.vendorId()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        else if (create) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Vendor is required.");
        if (create || StringUtils.hasText(request.contractNumber())) {
            String number = DomainSupport.text(request.contractNumber(), "Contract number");
            if ((create && contractRepository.existsByContractNumberIgnoreCase(number)) || (!create && contractRepository.existsByContractNumberIgnoreCaseAndIdNot(number, contract.getId()))) throw DomainSupport.conflict("Contract number is already in use.");
            contract.setContractNumber(number);
        }
        if (create || StringUtils.hasText(request.title())) contract.setTitle(DomainSupport.text(request.title(), "Contract title"));
        if (request.startDate() != null) contract.setStartDate(request.startDate());
        if (create && contract.getStartDate() == null) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Contract start date is required.");
        if (request.endDate() != null) contract.setEndDate(request.endDate());
        if (request.status() != null) contract.setStatus(request.status());
        if (request.contractValue() != null) contract.setContractValue(nonNegative(request.contractValue(), "Contract value"));
        if (request.currency() != null) contract.setCurrency(DomainSupport.currency(request.currency()));
        if (request.documentReference() != null) contract.setDocumentReference(DomainSupport.optionalText(request.documentReference()));
        if (request.notes() != null) contract.setNotes(DomainSupport.optionalText(request.notes()));
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(contract.getStartDate())) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Contract end date cannot be before start date.");
    }

    private BigDecimal calculateSubtotal(List<PurchaseOrderItem> items) {
        return items == null ? BigDecimal.ZERO : items.stream().map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nonNegative(BigDecimal value, String field) {
        if (value.signum() < 0) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, field + " cannot be negative.");
        return value;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private PurchaseOrderResponse orderResponse(PurchaseOrder order) {
        Vendor vendor = order.getVendor();
        Employee employee = order.getRequestedByEmployee();
        List<PurchaseOrderItemResponse> items = order.getItems() == null ? List.of() : order.getItems().stream().map(item -> new PurchaseOrderItemResponse(item.getId(), item.getCategory() == null ? null : item.getCategory().getId(), item.getCategory() == null ? null : item.getCategory().getName(), item.getDescription(), item.getQuantity(), item.getReceivedQuantity(), item.getUnitCost())).toList();
        return new PurchaseOrderResponse(order.getId(), order.getPoNumber(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), employee == null ? null : employee.getId(), employee == null ? null : DomainSupport.fullName(employee.getFirstName(), employee.getLastName()), order.getOrderDate(), order.getExpectedDate(), order.getReceivedDate(), order.getStatus(), order.getCurrency(), order.getSubtotal(), order.getTaxAmount(), order.getTotalAmount(), order.getNotes(), items, order.getCreatedAt(), order.getUpdatedAt());
    }

    private InvoiceResponse invoiceResponse(Invoice invoice) {
        Vendor vendor = invoice.getVendor();
        PurchaseOrder order = invoice.getPurchaseOrder();
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), order == null ? null : order.getId(), order == null ? null : order.getPoNumber(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), invoice.getInvoiceDate(), invoice.getDueDate(), invoice.getPaidDate(), invoice.getStatus(), invoice.getCurrency(), invoice.getSubtotal(), invoice.getTaxAmount(), invoice.getTotalAmount(), invoice.getNotes(), invoice.getCreatedAt(), invoice.getUpdatedAt());
    }

    private VendorContractResponse contractResponse(VendorContract contract) {
        Vendor vendor = contract.getVendor();
        return new VendorContractResponse(contract.getId(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), contract.getContractNumber(), contract.getTitle(), contract.getStartDate(), contract.getEndDate(), contract.getStatus(), contract.getContractValue(), contract.getCurrency(), contract.getDocumentReference(), contract.getNotes(), contract.getCreatedAt(), contract.getUpdatedAt());
    }
}
