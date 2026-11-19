package ru.vlad.orm.quiz;

import jakarta.persistence.*;
import java.time.Instant;
import ru.vlad.orm.catalog.User;

@Entity
@Table(name = "quiz_submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "quiz_id"}))
class QuizSubmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    Quiz quiz;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    User student;
    @Column(nullable = false) int score;
    @Column(nullable = false) int maxScore;
    @Column(nullable = false) Instant takenAt;

    protected QuizSubmission() {}
}
