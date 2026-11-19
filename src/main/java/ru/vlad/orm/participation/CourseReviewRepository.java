package ru.vlad.orm.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    List<CourseReview> findAllByCourseIdOrderById(Long courseId);
}
