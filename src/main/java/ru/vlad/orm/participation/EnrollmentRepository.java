package ru.vlad.orm.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findAllByCourseIdOrderById(Long courseId);
    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findAllByStudentIdOrderById(Long studentId);
}
