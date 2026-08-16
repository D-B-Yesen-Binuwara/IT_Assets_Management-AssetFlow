package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.AuthResponse;
import com.binuwara.AssetsFlow.DTO.LoginRequest;
import com.binuwara.AssetsFlow.DTO.UserResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.AuditLog;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.UserStatus;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            EmployeeRepository employeeRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        var employee = employeeRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::invalidCredentials);

        AppUser user = appUserRepository.findByEmployee_Id(employee.getId())
                .orElseThrow(this::invalidCredentials);

        if (user.getStatus() != UserStatus.ACTIVE
                || employee.getStatus() == EmployeeStatus.INACTIVE
                || employee.getStatus() == EmployeeStatus.TERMINATED
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        user.setLastLoginAt(Instant.now());
        AppUser saved = appUserRepository.save(user);
        JwtService.JwtToken token = jwtService.createToken(saved.getId());

        AuditLog auditLog = new AuditLog();
        auditLog.setActorUser(saved);
        auditLog.setEntityType("APP_USER");
        auditLog.setEntityId(saved.getId());
        auditLog.setAction("LOGIN");
        auditLogRepository.save(auditLog);

        return new LoginResult(
                token.value(),
                new AuthResponse("Login successful.", token.expiresAt(), UserResponse.from(saved))
        );
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(AuthenticatedUser principal) {
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return appUserRepository.findWithIdentityById(principal.getId())
                .map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account is no longer available."));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
    }

    public record LoginResult(String token, AuthResponse response) {
    }
}
