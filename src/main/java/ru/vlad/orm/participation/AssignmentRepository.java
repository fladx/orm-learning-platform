package ru.vlad.orm.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByLessonIdOrderById(Long lessonId);
}
