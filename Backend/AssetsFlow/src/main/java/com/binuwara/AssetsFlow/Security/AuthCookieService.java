package com.binuwara.AssetsFlow.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthCookieService {
    private final String cookieName;
    private final boolean secure;

    public AuthCookieService(
            @Value("${app.security.cookie-name:ASSETFLOW_AUTH}") String cookieName,
            @Value("${app.security.cookie-secure:false}") boolean secure
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void addAuthenticationCookie(
            HttpServletResponse response,
            String token,
            Instant expiresAt
    ) {
        long seconds = Math.max(1, Duration.between(Instant.now(), expiresAt).toSeconds());
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(seconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthenticationCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
