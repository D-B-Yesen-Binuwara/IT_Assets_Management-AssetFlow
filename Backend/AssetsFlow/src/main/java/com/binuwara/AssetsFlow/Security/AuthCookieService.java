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
    private final String sameSite;
    private final String domain;

    public AuthCookieService(
            @Value("${app.security.cookie-name:ASSETFLOW_AUTH}") String cookieName,
            @Value("${app.security.cookie-secure:false}") boolean secure,
            @Value("${app.security.cookie-same-site:Lax}") String sameSite,
            @Value("${app.security.cookie-domain:}") String domain
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.domain = domain;
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
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(seconds));
        if (domain != null && !domain.isBlank()) builder.domain(domain.trim());
        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthenticationCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO);
        if (domain != null && !domain.isBlank()) builder.domain(domain.trim());
        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
