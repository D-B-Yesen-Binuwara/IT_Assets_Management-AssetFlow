package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.CreateAccountRequest;
import com.binuwara.AssetsFlow.DTO.UpdateUserRequest;
import com.binuwara.AssetsFlow.DTO.UserResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.UserAccountService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<List<UserResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return ResponseEntity.ok(userAccountService.listVisibleUsers(actor));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<UserResponse> get(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return ResponseEntity.ok(userAccountService.getVisibleUser(userId, actor));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userAccountService.createAccount(request, actor));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return ResponseEntity.ok(userAccountService.update(userId, request, actor));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        userAccountService.disable(userId, actor);
        return ResponseEntity.noContent().build();
    }
}
