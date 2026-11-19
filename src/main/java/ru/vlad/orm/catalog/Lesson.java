package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "lessons")
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 10000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Module module;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    void setContent(String content) { this.content = content; }
    public Module getModule() { return module; }
    void setModule(Module module) { this.module = module; }
}
