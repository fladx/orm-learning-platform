package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
    void setName(String name) { this.name = name; }
}
