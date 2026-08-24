package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.CategoryRequest;
import com.binuwara.AssetsFlow.DTO.CategoryResponse;
import com.binuwara.AssetsFlow.DTO.DepartmentRequest;
import com.binuwara.AssetsFlow.DTO.DepartmentResponse;
import com.binuwara.AssetsFlow.DTO.EmployeeRequest;
import com.binuwara.AssetsFlow.DTO.EmployeeResponse;
import com.binuwara.AssetsFlow.DTO.LocationRequest;
import com.binuwara.AssetsFlow.DTO.LocationResponse;
import com.binuwara.AssetsFlow.DTO.VendorRequest;
import com.binuwara.AssetsFlow.DTO.VendorResponse;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.CatalogService;
import jakarta.validation.Valid;
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
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() { return catalogService.listCategories(); }

    @GetMapping("/categories/{id}")
    public CategoryResponse category(@PathVariable UUID id) { return catalogService.getCategory(id); }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCategory(request, actor)); }

    @PatchMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public CategoryResponse updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return catalogService.updateCategory(id, request, actor); }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { catalogService.deleteCategory(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/departments")
    public List<DepartmentResponse> departments() { return catalogService.listDepartments(); }

    @GetMapping("/departments/{id}")
    public DepartmentResponse department(@PathVariable UUID id) { return catalogService.getDepartment(id); }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createDepartment(request, actor)); }

    @PatchMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public DepartmentResponse updateDepartment(@PathVariable UUID id, @RequestBody DepartmentRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return catalogService.updateDepartment(id, request, actor); }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { catalogService.deleteDepartment(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/employees")
    public List<EmployeeResponse> employees(@RequestParam(required = false) EmployeeStatus status) { return catalogService.listEmployees(status); }

    @GetMapping("/employees/{id}")
    public EmployeeResponse employee(@PathVariable UUID id) { return catalogService.getEmployee(id); }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createEmployee(request, actor)); }

    @PatchMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public EmployeeResponse updateEmployee(@PathVariable UUID id, @RequestBody EmployeeRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return catalogService.updateEmployee(id, request, actor); }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { catalogService.deleteEmployee(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/locations")
    public List<LocationResponse> locations() { return catalogService.listLocations(); }

    @GetMapping("/locations/{id}")
    public LocationResponse location(@PathVariable UUID id) { return catalogService.getLocation(id); }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<LocationResponse> createLocation(@RequestBody LocationRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createLocation(request, actor)); }

    @PatchMapping("/locations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public LocationResponse updateLocation(@PathVariable UUID id, @RequestBody LocationRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return catalogService.updateLocation(id, request, actor); }

    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { catalogService.deleteLocation(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/vendors")
    public List<VendorResponse> vendors() { return catalogService.listVendors(); }

    @GetMapping("/vendors/{id}")
    public VendorResponse vendor(@PathVariable UUID id) { return catalogService.getVendor(id); }

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<VendorResponse> createVendor(@RequestBody VendorRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createVendor(request, actor)); }

    @PatchMapping("/vendors/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public VendorResponse updateVendor(@PathVariable UUID id, @RequestBody VendorRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return catalogService.updateVendor(id, request, actor); }

    @DeleteMapping("/vendors/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteVendor(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { catalogService.deleteVendor(id, actor); return ResponseEntity.noContent().build(); }
}
