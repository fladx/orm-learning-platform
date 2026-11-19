package ru.vlad.orm.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    boolean existsByQuizId(Long quizId);
    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);
    List<QuizSubmission> findByStudentIdOrderById(Long studentId);
    List<QuizSubmission> findByQuizModuleCourseIdOrderById(Long courseId);
    List<QuizSubmission> findByQuizModuleIdOrderById(Long moduleId);
    List<QuizSubmission> findByQuizIdOrderById(Long quizId);
}
