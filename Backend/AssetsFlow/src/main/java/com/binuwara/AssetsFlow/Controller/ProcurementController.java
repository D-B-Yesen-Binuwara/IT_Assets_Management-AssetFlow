package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.InvoiceRequest;
import com.binuwara.AssetsFlow.DTO.InvoiceResponse;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderRequest;
import com.binuwara.AssetsFlow.DTO.PurchaseOrderResponse;
import com.binuwara.AssetsFlow.DTO.VendorContractRequest;
import com.binuwara.AssetsFlow.DTO.VendorContractResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.ProcurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProcurementController {
    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) { this.procurementService = procurementService; }

    @GetMapping("/procurement")
    public List<PurchaseOrderResponse> orders(@RequestParam(required = false) String status) { return procurementService.listOrders(status); }

    @GetMapping("/procurement/{id}")
    public PurchaseOrderResponse order(@PathVariable UUID id) { return procurementService.getOrder(id); }

    @PostMapping("/procurement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<PurchaseOrderResponse> createOrder(@RequestBody PurchaseOrderRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(procurementService.createOrder(request, actor)); }

    @PatchMapping("/procurement/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public PurchaseOrderResponse updateOrder(@PathVariable UUID id, @RequestBody PurchaseOrderRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return procurementService.updateOrder(id, request, actor); }

    @DeleteMapping("/procurement/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { procurementService.deleteOrder(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices() { return procurementService.listInvoices(); }

    @GetMapping("/invoices/{id}")
    public InvoiceResponse invoice(@PathVariable UUID id) { return procurementService.getInvoice(id); }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody InvoiceRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(procurementService.createInvoice(request, actor)); }

    @PatchMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public InvoiceResponse updateInvoice(@PathVariable UUID id, @RequestBody InvoiceRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return procurementService.updateInvoice(id, request, actor); }

    @DeleteMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { procurementService.deleteInvoice(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/vendor-contracts")
    public List<VendorContractResponse> contracts(@RequestParam(required = false) UUID vendorId) { return procurementService.listContracts(vendorId); }

    @GetMapping("/vendor-contracts/{id}")
    public VendorContractResponse contract(@PathVariable UUID id) { return procurementService.getContract(id); }

    @PostMapping("/vendor-contracts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<VendorContractResponse> createContract(@RequestBody VendorContractRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(procurementService.createContract(request, actor)); }

    @PatchMapping("/vendor-contracts/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public VendorContractResponse updateContract(@PathVariable UUID id, @RequestBody VendorContractRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return procurementService.updateContract(id, request, actor); }

    @DeleteMapping("/vendor-contracts/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { procurementService.deleteContract(id, actor); return ResponseEntity.noContent().build(); }
}
