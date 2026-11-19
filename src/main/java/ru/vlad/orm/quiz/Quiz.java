package ru.vlad.orm.quiz;

import jakarta.persistence.*;
import ru.vlad.orm.catalog.Module;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    Module module;
    @Column(nullable = false, length = 150) String title;
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    List<Question> questions = new ArrayList<>();

    protected Quiz() {}
    public Long getId() { return id; }
}
