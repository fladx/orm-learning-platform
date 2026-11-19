package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "course_modules")
public class Module {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Course course;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("id ASC")
    private List<Lesson> lessons = new ArrayList<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    void setTitle(String title) { this.title = title; }
    public Course getCourse() { return course; }
    void setCourse(Course course) { this.course = course; }
    public List<Lesson> getLessons() { return lessons; }
}
