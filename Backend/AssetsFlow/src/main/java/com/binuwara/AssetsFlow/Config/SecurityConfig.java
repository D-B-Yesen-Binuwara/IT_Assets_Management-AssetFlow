package com.binuwara.AssetsFlow.Config;

import com.binuwara.AssetsFlow.Security.JwtAuthenticationFilter;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.UserStatus;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Security.RoleNames;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
        return username -> appUserRepository.findByUsernameIgnoreCase(username)
                .map(user -> {
                    boolean employeeEnabled = user.getEmployee() == null
                            || (user.getEmployee().getStatus() != EmployeeStatus.INACTIVE
                            && user.getEmployee().getStatus() != EmployeeStatus.TERMINATED);
                    String[] authorities = user.getRoles().stream()
                            .map(role -> "ROLE_" + RoleNames.normalize(role.getName()))
                            .toArray(String[]::new);
                    return User.withUsername(user.getUsername())
                            .password(user.getPasswordHash())
                            .authorities(authorities)
                            .disabled(user.getStatus() != UserStatus.ACTIVE || !employeeEnabled)
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Account was not found."));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                postRequest("/api/auth/login"),
                                postRequest("/api/auth/bootstrap")
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/bootstrap",
                                "/api/auth/csrf"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authenticationException) ->
                                writeSecurityError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication is required."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeSecurityError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "You do not have permission to perform this action."))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Accept", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeSecurityError(
            HttpServletResponse response,
            int status,
            String message
    ) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":" + status + ",\"message\":\"" + message + "\"}"
        );
    }

    private RequestMatcher postRequest(String path) {
        return request -> HttpMethod.POST.matches(request.getMethod())
                && path.equals(request.getServletPath());
    }
}
