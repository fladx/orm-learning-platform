package ru.vlad.orm.participation;

import jakarta.persistence.*;
import ru.vlad.orm.catalog.Lesson;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    protected Assignment() {}

    Assignment(Lesson lesson, String title, String description) {
        this.lesson = lesson;
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public Lesson getLesson() { return lesson; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
