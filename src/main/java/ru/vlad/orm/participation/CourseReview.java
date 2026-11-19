package ru.vlad.orm.participation;

import jakarta.persistence.*;
import ru.vlad.orm.catalog.Course;
import ru.vlad.orm.catalog.User;

@Entity
@Table(name = "course_reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class CourseReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    protected CourseReview() {}

    CourseReview(User student, Course course, int rating, String comment) {
        this.student = student;
        this.course = course;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public User getStudent() { return student; }
    public Course getCourse() { return course; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }

    void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
