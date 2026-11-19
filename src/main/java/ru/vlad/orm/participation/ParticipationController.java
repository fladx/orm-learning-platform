package ru.vlad.orm.participation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.vlad.orm.participation.ParticipationService.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ParticipationController {
    private final ParticipationService service;

    public ParticipationController(ParticipationService service) { this.service = service; }

    @PostMapping("/courses/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    EnrollmentView enroll(@PathVariable Long courseId, @Valid @RequestBody EnrollmentRequest request) {
        return service.enroll(courseId, request.studentId());
    }

    @DeleteMapping("/courses/{courseId}/enrollments/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unenroll(@PathVariable Long courseId, @PathVariable Long studentId) {
        service.unenroll(courseId, studentId);
    }

    @GetMapping("/courses/{courseId}/enrollments")
    List<EnrollmentView> courseEnrollments(@PathVariable Long courseId) {
        return service.courseEnrollments(courseId);
    }

    @GetMapping("/students/{studentId}/enrollments")
    List<EnrollmentView> studentEnrollments(@PathVariable Long studentId) {
        return service.studentEnrollments(studentId);
    }

    @PostMapping("/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    ReviewView createReview(@PathVariable Long courseId, @Valid @RequestBody ReviewRequest request) {
        return service.createReview(courseId, request.studentId(), request.rating(), request.comment());
    }

    @GetMapping("/courses/{courseId}/reviews")
    List<ReviewView> courseReviews(@PathVariable Long courseId) { return service.courseReviews(courseId); }

    @PutMapping("/reviews/{id}")
    ReviewView updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return service.updateReview(id, request.studentId(), request.rating(), request.comment());
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteReview(@PathVariable Long id, @RequestParam Long studentId) {
        service.deleteReview(id, studentId);
    }

    @PostMapping("/lessons/{lessonId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    AssignmentView createAssignment(@PathVariable Long lessonId, @Valid @RequestBody AssignmentRequest request) {
        return service.createAssignment(lessonId, request.teacherId(), request.title(), request.description());
    }

    @GetMapping("/lessons/{lessonId}/assignments")
    List<AssignmentView> lessonAssignments(@PathVariable Long lessonId) { return service.lessonAssignments(lessonId); }

    @GetMapping("/assignments/{id}")
    AssignmentView getAssignment(@PathVariable Long id) { return service.getAssignment(id); }

    @PutMapping("/assignments/{id}")
    AssignmentView updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return service.updateAssignment(id, request.teacherId(), request.title(), request.description());
    }

    @DeleteMapping("/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAssignment(@PathVariable Long id, @RequestParam Long teacherId) {
        service.deleteAssignment(id, teacherId);
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    SubmissionView submit(@PathVariable Long assignmentId, @Valid @RequestBody SubmissionRequest request) {
        return service.submit(assignmentId, request.studentId(), request.answer());
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    List<SubmissionView> assignmentSubmissions(@PathVariable Long assignmentId) {
        return service.assignmentSubmissions(assignmentId);
    }

    @GetMapping("/students/{studentId}/submissions")
    List<SubmissionView> studentSubmissions(@PathVariable Long studentId) { return service.studentSubmissions(studentId); }

    @GetMapping("/submissions/{id}")
    SubmissionView getSubmission(@PathVariable Long id) { return service.getSubmission(id); }

    @PutMapping("/submissions/{id}/grade")
    SubmissionView grade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return service.grade(id, request.teacherId(), request.grade(), request.feedback());
    }

    public record EnrollmentRequest(@NotNull @Positive Long studentId) {}
    public record ReviewRequest(@NotNull @Positive Long studentId, @NotNull @Min(1) @Max(5) Integer rating,
                                @NotBlank @Size(max = 2000) String comment) {}
    public record AssignmentRequest(@NotNull @Positive Long teacherId, @NotBlank @Size(max = 200) String title,
                                    @NotBlank @Size(max = 4000) String description) {}
    public record SubmissionRequest(@NotNull @Positive Long studentId, @NotBlank @Size(max = 8000) String answer) {}
    public record GradeRequest(@NotNull @Positive Long teacherId, @NotNull @Min(0) @Max(100) Integer grade,
                               @Size(max = 2000) String feedback) {}
}
