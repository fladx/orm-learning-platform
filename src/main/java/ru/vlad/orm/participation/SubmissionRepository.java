package ru.vlad.orm.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
    List<Submission> findAllByAssignmentIdOrderById(Long assignmentId);
    List<Submission> findAllByStudentIdOrderById(Long studentId);
    boolean existsByAssignmentId(Long assignmentId);
}
