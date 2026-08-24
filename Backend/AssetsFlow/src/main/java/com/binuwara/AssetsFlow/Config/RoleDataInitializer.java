package com.binuwara.AssetsFlow.Config;

import com.binuwara.AssetsFlow.Entity.Role;
import com.binuwara.AssetsFlow.Repository.RoleRepository;
import com.binuwara.AssetsFlow.Security.RoleNames;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RoleDataInitializer {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            Map<String, String> roles = Map.of(
                    RoleNames.SUPER_ADMIN, "Full system access.",
                    RoleNames.ADMIN, "Organization-wide operational administration.",
                    RoleNames.DEPARTMENT_HEAD, "Department-scoped administration.",
                    RoleNames.USER, "Standard application user."
            );

            roles.forEach((name, description) ->
                    roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                        Role role = new Role();
                        role.setName(name);
                        role.setDescription(description);
                        return roleRepository.save(role);
                    })
            );
        };
    }
}
