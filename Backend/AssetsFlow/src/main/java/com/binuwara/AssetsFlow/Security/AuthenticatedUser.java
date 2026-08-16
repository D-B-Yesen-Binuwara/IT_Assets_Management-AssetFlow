package com.binuwara.AssetsFlow.Security;

import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AuthenticatedUser implements UserDetails {
    private final UUID id;
    private final UUID employeeId;
    private final String email;
    private final String username;
    private final Set<GrantedAuthority> authorities;
    private final boolean enabled;

    private AuthenticatedUser(
            UUID id,
            UUID employeeId,
            String email,
            String username,
            Set<GrantedAuthority> authorities,
            boolean enabled
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.email = email;
        this.username = username;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static AuthenticatedUser from(AppUser user) {
        Employee employee = user.getEmployee();
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + RoleNames.normalize(role.getName())))
                .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedUser(
                user.getId(),
                employee == null ? null : employee.getId(),
                employee == null ? null : employee.getEmail(),
                user.getUsername(),
                authorities,
                user.getStatus().name().equals("ACTIVE")
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public String getEmail() {
        return email;
    }

    public boolean hasRole(String role) {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_" + RoleNames.normalize(role)));
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
