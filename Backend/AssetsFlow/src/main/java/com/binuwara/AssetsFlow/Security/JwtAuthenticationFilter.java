package com.binuwara.AssetsFlow.Security;

import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.UserStatus;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final AppUserRepository appUserRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AuthCookieService authCookieService,
            AppUserRepository appUserRepository
    ) {
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, authCookieService.getCookieName());
        if (cookie != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtService.JwtClaims claims = jwtService.parseAndValidate(cookie.getValue());
                appUserRepository.findWithIdentityById(claims.subject())
                        .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                        .filter(user -> user.getEmployee() == null
                                || (user.getEmployee().getStatus() != EmployeeStatus.INACTIVE
                                && user.getEmployee().getStatus() != EmployeeStatus.TERMINATED))
                        .ifPresent(user -> {
                            AuthenticatedUser principal = AuthenticatedUser.from(user);
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            } catch (JwtService.InvalidJwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
