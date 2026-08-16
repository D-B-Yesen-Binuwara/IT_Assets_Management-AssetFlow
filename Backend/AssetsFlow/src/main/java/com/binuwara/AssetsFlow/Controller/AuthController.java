package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.AuthResponse;
import com.binuwara.AssetsFlow.DTO.BootstrapAccountRequest;
import com.binuwara.AssetsFlow.DTO.CreateAccountRequest;
import com.binuwara.AssetsFlow.DTO.LoginRequest;
import com.binuwara.AssetsFlow.DTO.UserResponse;
import com.binuwara.AssetsFlow.Security.AuthCookieService;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.AuthService;
import com.binuwara.AssetsFlow.Service.UserAccountService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserAccountService userAccountService;
    private final AuthCookieService authCookieService;

    public AuthController(
            AuthService authService,
            UserAccountService userAccountService,
            AuthCookieService authCookieService
    ) {
        this.authService = authService;
        this.userAccountService = userAccountService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.LoginResult result = authService.login(request);
        authCookieService.addAuthenticationCookie(
                response,
                result.token(),
                result.response().expiresAt()
        );
        response.setHeader("Cache-Control", "no-store");
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<UserResponse> bootstrap(
            @Valid @RequestBody BootstrapAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userAccountService.bootstrap(request));
    }

    @PostMapping("/signup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> signup(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userAccountService.createAccount(request, actor));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookieService.clearAuthenticationCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(authService.currentUser(principal));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok(Map.of("token", token.getToken()));
    }
}
