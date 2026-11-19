package ru.vlad.orm.participation;

import jakarta.persistence.*;
import java.time.Instant;
import ru.vlad.orm.catalog.Course;
import ru.vlad.orm.catalog.User;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Instant enrolledAt = Instant.now();

    protected Enrollment() {}

    Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
    }

    public Long getId() { return id; }
    public User getStudent() { return student; }
    public Course getCourse() { return course; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
