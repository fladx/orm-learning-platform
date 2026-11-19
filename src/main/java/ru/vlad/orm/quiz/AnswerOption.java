package ru.vlad.orm.quiz;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_answer_options")
class AnswerOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;
    @Column(nullable = false, length = 250) String label;
    @Column(nullable = false) boolean correct;

    protected AnswerOption() {}
}
