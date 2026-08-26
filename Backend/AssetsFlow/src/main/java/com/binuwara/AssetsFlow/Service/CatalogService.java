package com.binuwara.AssetsFlow.Service;

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
import com.binuwara.AssetsFlow.Entity.AssetCategory;
import com.binuwara.AssetsFlow.Entity.Department;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.Location;
import com.binuwara.AssetsFlow.Entity.LocationType;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Entity.VendorStatus;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetCategoryRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.DepartmentRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.LocationRepository;
import com.binuwara.AssetsFlow.Repository.PurchaseOrderRepository;
import com.binuwara.AssetsFlow.Repository.VendorRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {
    private final AssetCategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;
    private final VendorRepository vendorRepository;
    private final AssetRepository assetRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public CatalogService(
            AssetCategoryRepository categoryRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            LocationRepository locationRepository,
            VendorRepository vendorRepository,
            AssetRepository assetRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AuditLogRepository auditLogRepository,
            AppUserRepository appUserRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.locationRepository = locationRepository;
        this.vendorRepository = vendorRepository;
        this.assetRepository = assetRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(this::categoryResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID id) {
        return categoryResponse(categoryRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Category")));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, AuthenticatedUser actor) {
        String name = DomainSupport.text(request.name(), "Category name");
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw DomainSupport.conflict("Category name is already in use.");
        }
        AssetCategory category = new AssetCategory();
        category.setName(name);
        category.setDescription(DomainSupport.optionalText(request.description()));
        if (request.active() != null) category.setActive(request.active());
        AssetCategory saved = categoryRepository.save(category);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_CATEGORY", saved.getId(), "CREATED", null);
        return categoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request, AuthenticatedUser actor) {
        AssetCategory category = categoryRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Category"));
        if (StringUtils.hasText(request.name())) {
            String name = request.name().trim();
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw DomainSupport.conflict("Category name is already in use.");
            category.setName(name);
        }
        if (request.description() != null) category.setDescription(DomainSupport.optionalText(request.description()));
        if (request.active() != null) category.setActive(request.active());
        AssetCategory saved = categoryRepository.save(category);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_CATEGORY", saved.getId(), "UPDATED", null);
        return categoryResponse(saved);
    }

    @Transactional
    public void deleteCategory(UUID id, AuthenticatedUser actor) {
        AssetCategory category = categoryRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Category"));
        category.setActive(false);
        categoryRepository.save(category);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_CATEGORY", id, "DEACTIVATED", null);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream().map(this::departmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(UUID id) {
        return departmentResponse(departmentRepository.findWithManagerById(id).orElseThrow(() -> DomainSupport.notFound("Department")));
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request, AuthenticatedUser actor) {
        String code = DomainSupport.text(request.code(), "Department code");
        String name = DomainSupport.text(request.name(), "Department name");
        if (departmentRepository.existsByCodeIgnoreCase(code) || departmentRepository.existsByNameIgnoreCase(name)) {
            throw DomainSupport.conflict("Department code or name is already in use.");
        }
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setDescription(DomainSupport.optionalText(request.description()));
        department.setManager(resolveEmployee(request.managerEmployeeId(), request.managerEmployeeNumber()));
        Department saved = departmentRepository.save(department);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "DEPARTMENT", saved.getId(), "CREATED", null);
        return departmentResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request, AuthenticatedUser actor) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Department"));
        if (StringUtils.hasText(request.code())) {
            String code = request.code().trim();
            if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) throw DomainSupport.conflict("Department code is already in use.");
            department.setCode(code);
        }
        if (StringUtils.hasText(request.name())) {
            String name = request.name().trim();
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw DomainSupport.conflict("Department name is already in use.");
            department.setName(name);
        }
        if (request.description() != null) department.setDescription(DomainSupport.optionalText(request.description()));
        if (request.managerEmployeeId() != null || StringUtils.hasText(request.managerEmployeeNumber())) {
            department.setManager(resolveEmployee(request.managerEmployeeId(), request.managerEmployeeNumber()));
        }
        Department saved = departmentRepository.save(department);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "DEPARTMENT", saved.getId(), "UPDATED", null);
        return departmentResponse(saved);
    }

    @Transactional
    public void deleteDepartment(UUID id, AuthenticatedUser actor) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Department"));
        if (!department.getEmployees().isEmpty() || !department.getAssets().isEmpty()) {
            throw DomainSupport.conflict("A department with employees or assets cannot be deleted.");
        }
        departmentRepository.delete(department);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "DEPARTMENT", id, "DELETED", null);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listEmployees(EmployeeStatus status) {
        return employeeRepository.findAllByOrderByEmployeeNumberAsc().stream()
                .filter(employee -> status == null || employee.getStatus() == status)
                .map(this::employeeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        return employeeResponse(employeeRepository.findWithDepartmentById(id).orElseThrow(() -> DomainSupport.notFound("Employee")));
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request, AuthenticatedUser actor) {
        String employeeNumber = DomainSupport.text(request.employeeNumber(), "Employee number");
        String email = DomainSupport.text(request.email(), "Email");
        if (employeeRepository.findByEmployeeNumberIgnoreCase(employeeNumber).isPresent()
                || employeeRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw DomainSupport.conflict("Employee number or email is already in use.");
        }
        Employee employee = new Employee();
        employee.setEmployeeNumber(employeeNumber);
        setEmployeeFields(employee, request, true);
        Employee saved = employeeRepository.save(employee);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "EMPLOYEE", saved.getId(), "CREATED", null);
        return employeeResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request, AuthenticatedUser actor) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Employee"));
        if (StringUtils.hasText(request.employeeNumber())) {
            String number = request.employeeNumber().trim();
            employeeRepository.findByEmployeeNumberIgnoreCase(number).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
                throw DomainSupport.conflict("Employee number is already in use.");
            });
            employee.setEmployeeNumber(number);
        }
        if (StringUtils.hasText(request.email())) {
            String email = request.email().trim();
            employeeRepository.findByEmailIgnoreCase(email).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
                throw DomainSupport.conflict("Email is already in use.");
            });
            employee.setEmail(email);
        }
        setEmployeeFields(employee, request, false);
        Employee saved = employeeRepository.save(employee);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "EMPLOYEE", saved.getId(), "UPDATED", null);
        return employeeResponse(saved);
    }

    @Transactional
    public void deleteEmployee(UUID id, AuthenticatedUser actor) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Employee"));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employee.setTerminationDate(LocalDate.now());
        employeeRepository.save(employee);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "EMPLOYEE", id, "DEACTIVATED", null);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> listLocations() {
        return locationRepository.findAllByOrderByNameAsc().stream().map(this::locationResponse).toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocation(UUID id) {
        return locationResponse(locationRepository.findWithParentById(id).orElseThrow(() -> DomainSupport.notFound("Location")));
    }

    @Transactional
    public LocationResponse createLocation(LocationRequest request, AuthenticatedUser actor) {
        Location location = new Location();
        String code = StringUtils.hasText(request.code()) ? request.code().trim() : generatedCode("LOC");
        if (locationRepository.existsByCodeIgnoreCase(code)) throw DomainSupport.conflict("Location code is already in use.");
        location.setCode(code);
        location.setName(DomainSupport.text(request.name(), "Location name"));
        location.setLocationType(DomainSupport.enumValue(DomainSupport.firstText(request.locationType(), request.type()), LocationType.class, LocationType.OTHER));
        location.setParentLocation(resolveLocation(request.parentLocationId(), request.parentLocation()));
        location.setAddressLine(DomainSupport.firstText(request.addressLine(), request.address()));
        location.setCity(DomainSupport.optionalText(request.city()));
        location.setCountry(DomainSupport.optionalText(request.country()));
        Location saved = locationRepository.save(location);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "LOCATION", saved.getId(), "CREATED", null);
        return locationResponse(saved);
    }

    @Transactional
    public LocationResponse updateLocation(UUID id, LocationRequest request, AuthenticatedUser actor) {
        Location location = locationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Location"));
        if (StringUtils.hasText(request.code())) {
            String code = request.code().trim();
            if (locationRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) throw DomainSupport.conflict("Location code is already in use.");
            location.setCode(code);
        }
        if (StringUtils.hasText(request.name())) location.setName(request.name().trim());
        if (StringUtils.hasText(request.locationType()) || StringUtils.hasText(request.type())) {
            location.setLocationType(DomainSupport.enumValue(DomainSupport.firstText(request.locationType(), request.type()), LocationType.class, location.getLocationType()));
        }
        if (request.parentLocationId() != null || StringUtils.hasText(request.parentLocation())) {
            if (id.equals(request.parentLocationId())) throw new ApiException(HttpStatus.BAD_REQUEST, "A location cannot be its own parent.");
            location.setParentLocation(resolveLocation(request.parentLocationId(), request.parentLocation()));
        }
        if (request.addressLine() != null || request.address() != null) location.setAddressLine(DomainSupport.firstText(request.addressLine(), request.address()));
        if (request.city() != null) location.setCity(DomainSupport.optionalText(request.city()));
        if (request.country() != null) location.setCountry(DomainSupport.optionalText(request.country()));
        Location saved = locationRepository.save(location);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "LOCATION", saved.getId(), "UPDATED", null);
        return locationResponse(saved);
    }

    @Transactional
    public void deleteLocation(UUID id, AuthenticatedUser actor) {
        Location location = locationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Location"));
        if (!location.getChildLocations().isEmpty() || assetRepository.countByLocation_Id(id) > 0) throw DomainSupport.conflict("A location with child locations or assets cannot be deleted.");
        locationRepository.delete(location);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "LOCATION", id, "DELETED", null);
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> listVendors() {
        return vendorRepository.findAllByOrderByNameAsc().stream().map(this::vendorResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(UUID id) {
        return vendorResponse(vendorRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor")));
    }

    @Transactional
    public VendorResponse createVendor(VendorRequest request, AuthenticatedUser actor) {
        String name = DomainSupport.text(request.name(), "Vendor name");
        if (vendorRepository.existsByNameIgnoreCase(name)) throw DomainSupport.conflict("Vendor name is already in use.");
        Vendor vendor = new Vendor();
        String code = StringUtils.hasText(request.vendorCode()) ? request.vendorCode().trim() : generatedCode("VEN");
        if (vendorRepository.existsByVendorCodeIgnoreCase(code)) throw DomainSupport.conflict("Vendor code is already in use.");
        vendor.setVendorCode(code);
        setVendorFields(vendor, request, true);
        Vendor saved = vendorRepository.save(vendor);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR", saved.getId(), "CREATED", null);
        return vendorResponse(saved);
    }

    @Transactional
    public VendorResponse updateVendor(UUID id, VendorRequest request, AuthenticatedUser actor) {
        Vendor vendor = vendorRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor"));
        if (StringUtils.hasText(request.vendorCode())) {
            String code = request.vendorCode().trim();
            if (vendorRepository.existsByVendorCodeIgnoreCaseAndIdNot(code, id)) throw DomainSupport.conflict("Vendor code is already in use.");
            vendor.setVendorCode(code);
        }
        if (StringUtils.hasText(request.name()) && vendorRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) throw DomainSupport.conflict("Vendor name is already in use.");
        setVendorFields(vendor, request, false);
        Vendor saved = vendorRepository.save(vendor);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR", saved.getId(), "UPDATED", null);
        return vendorResponse(saved);
    }

    @Transactional
    public void deleteVendor(UUID id, AuthenticatedUser actor) {
        Vendor vendor = vendorRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor"));
        vendor.setStatus(VendorStatus.INACTIVE);
        vendorRepository.save(vendor);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "VENDOR", id, "DEACTIVATED", null);
    }

    private void setEmployeeFields(Employee employee, EmployeeRequest request, boolean create) {
        String name = DomainSupport.optionalText(request.name());
        if (create || StringUtils.hasText(request.firstName()) || StringUtils.hasText(request.lastName()) || name != null) {
            String firstName = StringUtils.hasText(request.firstName()) ? request.firstName().trim() : null;
            String lastName = StringUtils.hasText(request.lastName()) ? request.lastName().trim() : null;
            if ((firstName == null || lastName == null) && name != null) {
                String[] parts = name.split("\\s+", 2);
                if (firstName == null) firstName = parts[0];
                if (lastName == null) lastName = parts.length > 1 ? parts[1] : parts[0];
            }
            employee.setFirstName(DomainSupport.text(firstName, "First name"));
            employee.setLastName(DomainSupport.text(lastName, "Last name"));
        }
        if (create || StringUtils.hasText(request.email())) employee.setEmail(DomainSupport.text(request.email(), "Email"));
        if (request.phone() != null) employee.setPhone(DomainSupport.optionalText(request.phone()));
        if (request.address() != null) employee.setAddress(DomainSupport.optionalText(request.address()));
        if (request.jobTitle() != null) employee.setJobTitle(DomainSupport.optionalText(request.jobTitle()));
        if (request.branchId() != null || StringUtils.hasText(request.branch())) employee.setBranch(resolveLocation(request.branchId(), request.branch()));
        else if (create) throw new ApiException(HttpStatus.BAD_REQUEST, "Branch is required.");
        if (request.departmentId() != null || request.department() != null) employee.setDepartment(resolveDepartment(request.departmentId(), request.department()));
        if (request.status() != null) employee.setStatus(request.status());
        if (request.hireDate() != null) employee.setHireDate(request.hireDate());
        if (request.terminationDate() != null) employee.setTerminationDate(request.terminationDate());
        if (employee.getTerminationDate() != null && employee.getHireDate() != null && employee.getTerminationDate().isBefore(employee.getHireDate())) throw new ApiException(HttpStatus.BAD_REQUEST, "Termination date cannot be before hire date.");
    }

    private void setVendorFields(Vendor vendor, VendorRequest request, boolean create) {
        if (create || StringUtils.hasText(request.name())) vendor.setName(DomainSupport.text(request.name(), "Vendor name"));
        if (request.contactName() != null || request.contact() != null) vendor.setContactName(DomainSupport.firstText(request.contactName(), request.contact()));
        if (request.email() != null) vendor.setEmail(DomainSupport.optionalText(request.email()));
        if (request.phone() != null) vendor.setPhone(DomainSupport.optionalText(request.phone()));
        if (request.category() != null) vendor.setCategory(DomainSupport.optionalText(request.category()));
        if (request.address() != null) vendor.setAddress(DomainSupport.optionalText(request.address()));
        if (request.status() != null) vendor.setStatus(request.status());
    }

    private Employee resolveEmployee(UUID id, String employeeNumber) {
        if (id != null) return employeeRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Employee"));
        if (StringUtils.hasText(employeeNumber)) return employeeRepository.findByEmployeeNumberIgnoreCase(employeeNumber.trim()).orElseThrow(() -> DomainSupport.notFound("Employee"));
        return null;
    }

    private Department resolveDepartment(UUID id, String value) {
        if (id != null) return departmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Department"));
        if (StringUtils.hasText(value)) return departmentRepository.findByCodeIgnoreCase(value.trim()).orElseGet(() -> departmentRepository.findByNameIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Department")));
        return null;
    }

    private Location resolveLocation(UUID id, String value) {
        if (id != null) return locationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Location"));
        if (StringUtils.hasText(value)) return locationRepository.findByCodeIgnoreCase(value.trim()).orElseGet(() -> locationRepository.findByNameIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Location")));
        return null;
    }

    private String generatedCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CategoryResponse categoryResponse(AssetCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.isActive(), assetRepository.countByCategory_Id(category.getId()), category.getCreatedAt(), category.getUpdatedAt());
    }

    private DepartmentResponse departmentResponse(Department department) {
        Employee manager = department.getManager();
        return new DepartmentResponse(department.getId(), department.getCode(), department.getName(), department.getDescription(), manager == null ? null : manager.getId(), manager == null ? null : DomainSupport.fullName(manager.getFirstName(), manager.getLastName()), department.getEmployees().size(), assetRepository.countByDepartment_Id(department.getId()), department.getCreatedAt(), department.getUpdatedAt());
    }

    private EmployeeResponse employeeResponse(Employee employee) {
        Department department = employee.getDepartment();
        Location branch = employee.getBranch();
        return new EmployeeResponse(employee.getId(), employee.getEmployeeNumber(), employee.getFirstName(), employee.getLastName(), DomainSupport.fullName(employee.getFirstName(), employee.getLastName()), employee.getEmail(), employee.getPhone(), employee.getAddress(), employee.getJobTitle(), branch == null ? null : branch.getId(), branch == null ? null : branch.getName(), department == null ? null : department.getId(), department == null ? null : department.getName(), employee.getStatus(), employee.getHireDate(), employee.getTerminationDate(), employee.getAppUser() != null, employee.getCreatedAt(), employee.getUpdatedAt());
    }

    private LocationResponse locationResponse(Location location) {
        Location parent = location.getParentLocation();
        return new LocationResponse(location.getId(), location.getCode(), location.getName(), location.getLocationType(), location.getLocationType(), parent == null ? null : parent.getId(), parent == null ? null : parent.getName(), location.getAddressLine(), location.getAddressLine(), location.getCity(), location.getCountry(), parent == null ? null : parent.getName(), assetRepository.countByLocation_Id(location.getId()), location.getCreatedAt(), location.getUpdatedAt());
    }

    private VendorResponse vendorResponse(Vendor vendor) {
        return new VendorResponse(vendor.getId(), vendor.getVendorCode(), vendor.getName(), vendor.getContactName(), vendor.getContactName(), vendor.getEmail(), vendor.getPhone(), vendor.getCategory(), vendor.getAddress(), vendor.getStatus(), assetRepository.countByVendor_Id(vendor.getId()), purchaseOrderRepository.countByVendor_Id(vendor.getId()), vendor.getCreatedAt(), vendor.getUpdatedAt());
    }
}
