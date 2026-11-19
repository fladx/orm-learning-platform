package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "app_users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    public Long getId() { return id; }
    public String getName() { return name; }
    void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    void setRole(Role role) { this.role = role; }
    public Profile getProfile() { return profile; }
    void setProfile(Profile profile) { this.profile = profile; }
}
