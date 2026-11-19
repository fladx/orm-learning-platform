package ru.vlad.orm.catalog;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User teacher;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("id ASC")
    private List<Module> modules = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY) @JoinTable(name = "course_tag", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    void setCategory(Category category) { this.category = category; }
    public User getTeacher() { return teacher; }
    void setTeacher(User teacher) { this.teacher = teacher; }
    public List<Module> getModules() { return modules; }
    public Set<Tag> getTags() { return tags; }
}
