package ru.vlad.orm.participation;

import jakarta.persistence.*;
import java.time.Instant;
import ru.vlad.orm.catalog.User;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "assignment_id"}))
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, updatable = false)
    private User student;

    @Column(nullable = false, length = 8000, updatable = false)
    private String answer;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    @Column
    private Integer grade;

    @Column(length = 2000)
    private String feedback;

    protected Submission() {}

    Submission(Assignment assignment, User student, String answer) {
        this.assignment = assignment;
        this.student = student;
        this.answer = answer;
    }

    public Long getId() { return id; }
    public Assignment getAssignment() { return assignment; }
    public User getStudent() { return student; }
    public String getAnswer() { return answer; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Integer getGrade() { return grade; }
    public String getFeedback() { return feedback; }

    void grade(int grade, String feedback) {
        this.grade = grade;
        this.feedback = feedback;
    }
}
