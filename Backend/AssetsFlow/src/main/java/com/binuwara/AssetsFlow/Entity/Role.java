package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<AppUser> users = new HashSet<>();

    @ManyToMany
    @jakarta.persistence.JoinTable(
            name = "role_permissions",
            joinColumns = @jakarta.persistence.JoinColumn(name = "role_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
