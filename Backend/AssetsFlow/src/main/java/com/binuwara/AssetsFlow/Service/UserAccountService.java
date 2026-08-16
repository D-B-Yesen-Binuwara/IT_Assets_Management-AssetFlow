package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.BootstrapAccountRequest;
import com.binuwara.AssetsFlow.DTO.CreateAccountRequest;
import com.binuwara.AssetsFlow.DTO.UpdateUserRequest;
import com.binuwara.AssetsFlow.DTO.UserResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.AuditLog;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.Role;
import com.binuwara.AssetsFlow.Entity.UserStatus;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.RoleRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Security.RoleNames;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserAccountService {
    private static final Set<String> SUPPORTED_ROLES = Set.of(
            RoleNames.SUPER_ADMIN,
            RoleNames.ADMIN,
            RoleNames.DEPARTMENT_HEAD,
            RoleNames.USER
    );

    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            AppUserRepository appUserRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createAccount(
            CreateAccountRequest request,
            AuthenticatedUser actor
    ) {
        String roleName = RoleNames.normalize(request.role());
        validatePasswordPair(request.password(), request.confirmPassword());
        return createAccount(
                request.email(),
                request.employeeId(),
                request.username(),
                request.password(),
                roleName,
                actor
        );
    }

    @Transactional
    public UserResponse bootstrap(BootstrapAccountRequest request) {
        if (appUserRepository.count() > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Bootstrap is disabled because an account already exists."
            );
        }

        validatePasswordPair(request.password(), request.confirmPassword());
        return createAccount(
                request.email(),
                request.employeeId(),
                request.username(),
                request.password(),
                RoleNames.SUPER_ADMIN,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listVisibleUsers(AuthenticatedUser actor) {
        requireManager(actor);
        return appUserRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(user -> actor.hasRole(RoleNames.SUPER_ADMIN)
                        || actor.getId().equals(user.getId())
                        || canManageTarget(actor, user))
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getVisibleUser(UUID userId, AuthenticatedUser actor) {
        requireManager(actor);
        AppUser user = findUser(userId);
        if (!actor.hasRole(RoleNames.SUPER_ADMIN)
                && !actor.getId().equals(user.getId())
                && !canManageTarget(actor, user)) {
            throw forbidden();
        }
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(
            UUID userId,
            UpdateUserRequest request,
            AuthenticatedUser actor
    ) {
        AppUser target = findUser(userId);
        boolean selfUpdate = actor.getId().equals(target.getId());

        if (selfUpdate) {
            if (request.status() != null || StringUtils.hasText(request.role())) {
                throw forbidden("You may update your username or password, but not your status or role.");
            }
        } else {
            requireManager(actor);
            if (!canManageTarget(actor, target)) {
                throw forbidden();
            }
        }

        if (StringUtils.hasText(request.username())) {
            String username = request.username().trim();
            if (appUserRepository.existsByUsernameIgnoreCaseAndIdNot(username, target.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Username is already in use.");
            }
            target.setUsername(username);
        }

        if (StringUtils.hasText(request.password())) {
            validatePasswordPair(request.password(), request.confirmPassword());
            target.setPasswordHash(passwordEncoder.encode(request.password()));
        } else if (StringUtils.hasText(request.confirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password and confirmation must be provided together.");
        }

        if (!selfUpdate && request.status() != null) {
            if (request.status() == UserStatus.DISABLED
                    && hasRole(target, RoleNames.SUPER_ADMIN)
                    && countActiveSuperAdmins() <= 1) {
                throw new ApiException(HttpStatus.CONFLICT, "The last Super Admin cannot be disabled.");
            }
            target.setStatus(request.status());
        }

        if (!selfUpdate && StringUtils.hasText(request.role())) {
            String roleName = RoleNames.normalize(request.role());
            assertCanAssignRole(actor, roleName, target.getEmployee());
            target.setRoles(new HashSet<>(Set.of(findRole(roleName))));
        }

        AppUser saved = appUserRepository.save(target);
        audit(actor, "USER_UPDATED", saved.getId(), roleNameForAudit(saved));
        return UserResponse.from(saved);
    }

    @Transactional
    public void disable(UUID userId, AuthenticatedUser actor) {
        AppUser target = findUser(userId);
        if (actor.getId().equals(target.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "You cannot delete your own account.");
        }
        requireManager(actor);
        if (!canManageTarget(actor, target)) {
            throw forbidden();
        }
        if (hasRole(target, RoleNames.SUPER_ADMIN) && countActiveSuperAdmins() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "The last Super Admin cannot be disabled.");
        }

        target.setStatus(UserStatus.DISABLED);
        appUserRepository.save(target);
        audit(actor, "USER_DISABLED", target.getId(), roleNameForAudit(target));
    }

    @Transactional(readOnly = true)
    public AppUser findUser(UUID userId) {
        return appUserRepository.findWithIdentityById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account was not found."));
    }

    private UserResponse createAccount(
            String email,
            String employeeId,
            String username,
            String password,
            String roleName,
            AuthenticatedUser actor
    ) {
        if (actor != null) {
            assertCanAssignRole(actor, roleName, null);
        } else if (!RoleNames.SUPER_ADMIN.equals(roleName)) {
            throw forbidden();
        }

        Employee employee = employeeRepository.findByEmployeeNumberIgnoreCase(employeeId.trim())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "No employee matches the supplied Employee ID."
                ));

        if (!employee.getEmail().equalsIgnoreCase(email.trim())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "The supplied email does not match the employee record."
            );
        }
        if (employee.getStatus() == EmployeeStatus.INACTIVE
                || employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new ApiException(HttpStatus.CONFLICT, "This employee is not eligible for an account.");
        }
        if (actor != null && actor.hasRole(RoleNames.DEPARTMENT_HEAD)) {
            assertSameDepartment(actor, employee);
        }
        if (appUserRepository.findByEmployee_Id(employee.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "This employee already has an account.");
        }

        String normalizedUsername = username.trim();
        if (appUserRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already in use.");
        }

        AppUser user = new AppUser();
        user.setEmployee(employee);
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(findRole(roleName))));

        AppUser saved = appUserRepository.save(user);
        audit(actor, "USER_CREATED", saved.getId(), roleName);
        return UserResponse.from(saved);
    }

    private void requireManager(AuthenticatedUser actor) {
        if (actor == null || (!actor.hasRole(RoleNames.SUPER_ADMIN)
                && !actor.hasRole(RoleNames.ADMIN)
                && !actor.hasRole(RoleNames.DEPARTMENT_HEAD))) {
            throw forbidden();
        }
    }

    private void assertCanAssignRole(
            AuthenticatedUser actor,
            String roleName,
            Employee targetEmployee
    ) {
        if (actor == null) {
            throw forbidden();
        }
        if (!SUPPORTED_ROLES.contains(roleName)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The requested role is not supported.");
        }

        boolean allowed = actor.hasRole(RoleNames.SUPER_ADMIN)
                || (actor.hasRole(RoleNames.ADMIN)
                && Set.of(RoleNames.DEPARTMENT_HEAD, RoleNames.USER).contains(roleName))
                || (actor.hasRole(RoleNames.DEPARTMENT_HEAD)
                && RoleNames.USER.equals(roleName));

        if (!allowed) {
            throw forbidden("Your role cannot create or assign the requested role.");
        }

        if (targetEmployee != null && actor.hasRole(RoleNames.DEPARTMENT_HEAD)) {
            assertSameDepartment(actor, targetEmployee);
        }
    }

    private boolean canManageTarget(AuthenticatedUser actor, AppUser target) {
        if (actor.hasRole(RoleNames.SUPER_ADMIN)) {
            return true;
        }

        if (hasRole(target, RoleNames.SUPER_ADMIN) || hasRole(target, RoleNames.ADMIN)) {
            return false;
        }

        if (actor.hasRole(RoleNames.ADMIN)) {
            return hasRole(target, RoleNames.DEPARTMENT_HEAD)
                    || hasRole(target, RoleNames.USER);
        }

        return actor.hasRole(RoleNames.DEPARTMENT_HEAD)
                && hasRole(target, RoleNames.USER)
                && target.getEmployee() != null
                && sameDepartment(actor, target.getEmployee());
    }

    private boolean sameDepartment(AuthenticatedUser actor, Employee employee) {
        if (actor.getEmployeeId() == null || employee == null) {
            return false;
        }
        Employee actorEmployee = employeeRepository.findById(actor.getEmployeeId()).orElse(null);
        return actorEmployee != null
                && actorEmployee.getDepartment() != null
                && employee.getDepartment() != null
                && actorEmployee.getDepartment().getId().equals(employee.getDepartment().getId());
    }

    private void assertSameDepartment(AuthenticatedUser actor, Employee employee) {
        if (!sameDepartment(actor, employee)) {
            throw forbidden("Department Heads may manage accounts only in their own department.");
        }
    }

    private Role findRole(String roleName) {
        return roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "The requested role is not configured."
                ));
    }

    private boolean hasRole(AppUser user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> RoleNames.normalize(role.getName()).equals(roleName));
    }

    private long countActiveSuperAdmins() {
        return appUserRepository.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> hasRole(user, RoleNames.SUPER_ADMIN))
                .count();
    }

    private void validatePasswordPair(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password and confirmation do not match.");
        }
    }

    private String roleNameForAudit(AppUser user) {
        return user.getRoles().stream()
                .map(role -> RoleNames.normalize(role.getName()))
                .findFirst()
                .orElse("NONE");
    }

    private void audit(
            AuthenticatedUser actor,
            String action,
            UUID entityId,
            String role
    ) {
        AuditLog auditLog = new AuditLog();
        if (actor != null) {
            auditLog.setActorUser(appUserRepository.getReferenceById(actor.getId()));
        }
        auditLog.setEntityType("APP_USER");
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setNewValues("{\"role\":\"" + role + "\"}");
        auditLogRepository.save(auditLog);
    }

    private ApiException forbidden() {
        return forbidden("You do not have permission to manage this account.");
    }

    private ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
