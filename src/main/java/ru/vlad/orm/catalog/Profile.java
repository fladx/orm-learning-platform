package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    public Long getId() { return id; }
    public User getUser() { return user; }
    void setUser(User user) { this.user = user; }
    public String getBio() { return bio; }
    void setBio(String bio) { this.bio = bio; }
}
