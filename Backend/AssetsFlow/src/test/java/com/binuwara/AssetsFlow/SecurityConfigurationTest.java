package com.binuwara.AssetsFlow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SecurityConfigurationTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void allowsTheConfiguredFrontendToUseCookieAndCsrfHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/assets");
        request.addHeader("Origin", "http://localhost:5173");

        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);

        assertTrue(configuration.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
        assertTrue(configuration.getAllowedHeaders().contains("X-XSRF-TOKEN"));
    }
}
