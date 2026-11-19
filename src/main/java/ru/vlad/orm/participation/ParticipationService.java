package ru.vlad.orm.participation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.orm.catalog.*;
import ru.vlad.orm.support.ApiException;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParticipationService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final CourseReviewRepository reviews;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;

    public ParticipationService(UserRepository users, CourseRepository courses, LessonRepository lessons,
                                EnrollmentRepository enrollments, CourseReviewRepository reviews,
                                AssignmentRepository assignments, SubmissionRepository submissions) {
        this.users = users;
        this.courses = courses;
        this.lessons = lessons;
        this.enrollments = enrollments;
        this.reviews = reviews;
        this.assignments = assignments;
        this.submissions = submissions;
    }

    @Transactional
    public EnrollmentView enroll(Long courseId, Long studentId) {
        Course course = course(courseId);
        User student = student(studentId);
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId))
            throw ApiException.conflict("Student is already enrolled");
        return view(enrollments.save(new Enrollment(student, course)));
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        enrollments.delete(enrollments.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> ApiException.notFound("Enrollment not found")));
    }

    public List<EnrollmentView> courseEnrollments(Long courseId) {
        course(courseId);
        return enrollments.findAllByCourseIdOrderById(courseId).stream().map(ParticipationService::view).toList();
    }

    public List<EnrollmentView> studentEnrollments(Long studentId) {
        student(studentId);
        return enrollments.findAllByStudentIdOrderById(studentId).stream().map(ParticipationService::view).toList();
    }

    @Transactional
    public ReviewView createReview(Long courseId, Long studentId, int rating, String comment) {
        Course course = course(courseId);
        User student = student(studentId);
        mustBeEnrolled(studentId, courseId);
        if (reviews.existsByStudentIdAndCourseId(studentId, courseId))
            throw ApiException.conflict("Student already reviewed this course");
        return view(reviews.save(new CourseReview(student, course, rating, comment)));
    }

    public List<ReviewView> courseReviews(Long courseId) {
        course(courseId);
        return reviews.findAllByCourseIdOrderById(courseId).stream().map(ParticipationService::view).toList();
    }

    @Transactional
    public ReviewView updateReview(Long reviewId, Long studentId, int rating, String comment) {
        CourseReview review = ownedReview(reviewId, studentId);
        review.update(rating, comment);
        return view(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long studentId) {
        reviews.delete(ownedReview(reviewId, studentId));
    }

    @Transactional
    public AssignmentView createAssignment(Long lessonId, Long teacherId, String title, String description) {
        Lesson lesson = lesson(lessonId);
        verifyTeacher(lesson, teacherId);
        return view(assignments.save(new Assignment(lesson, title, description)));
    }

    public List<AssignmentView> lessonAssignments(Long lessonId) {
        lesson(lessonId);
        return assignments.findAllByLessonIdOrderById(lessonId).stream().map(ParticipationService::view).toList();
    }

    public AssignmentView getAssignment(Long id) { return view(assignment(id)); }

    @Transactional
    public AssignmentView updateAssignment(Long id, Long teacherId, String title, String description) {
        Assignment assignment = assignment(id);
        verifyTeacher(assignment.getLesson(), teacherId);
        assignment.update(title, description);
        return view(assignment);
    }

    @Transactional
    public void deleteAssignment(Long id, Long teacherId) {
        Assignment assignment = assignment(id);
        verifyTeacher(assignment.getLesson(), teacherId);
        if (submissions.existsByAssignmentId(id)) throw ApiException.conflict("Assignment has submissions");
        assignments.delete(assignment);
    }

    @Transactional
    public SubmissionView submit(Long assignmentId, Long studentId, String answer) {
        Assignment assignment = assignment(assignmentId);
        User student = student(studentId);
        mustBeEnrolled(studentId, assignment.getLesson().getModule().getCourse().getId());
        if (submissions.existsByStudentIdAndAssignmentId(studentId, assignmentId))
            throw ApiException.conflict("Only one submission per assignment is allowed");
        return view(submissions.save(new Submission(assignment, student, answer)));
    }

    @Transactional
    public SubmissionView grade(Long id, Long teacherId, int grade, String feedback) {
        Submission submission = submission(id);
        verifyTeacher(submission.getAssignment().getLesson(), teacherId);
        submission.grade(grade, feedback);
        return view(submission);
    }

    public SubmissionView getSubmission(Long id) { return view(submission(id)); }

    public List<SubmissionView> assignmentSubmissions(Long assignmentId) {
        assignment(assignmentId);
        return submissions.findAllByAssignmentIdOrderById(assignmentId).stream().map(ParticipationService::view).toList();
    }

    public List<SubmissionView> studentSubmissions(Long studentId) {
        student(studentId);
        return submissions.findAllByStudentIdOrderById(studentId).stream().map(ParticipationService::view).toList();
    }

    private User student(Long id) {
        User user = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.getRole() != Role.STUDENT) throw ApiException.badRequest("User must be a student");
        return user;
    }

    private Course course(Long id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course not found"));
    }

    private Lesson lesson(Long id) {
        return lessons.findById(id).orElseThrow(() -> ApiException.notFound("Lesson not found"));
    }

    private Assignment assignment(Long id) {
        return assignments.findById(id).orElseThrow(() -> ApiException.notFound("Assignment not found"));
    }

    private Submission submission(Long id) {
        return submissions.findById(id).orElseThrow(() -> ApiException.notFound("Submission not found"));
    }

    private CourseReview ownedReview(Long id, Long studentId) {
        CourseReview review = reviews.findById(id).orElseThrow(() -> ApiException.notFound("Review not found"));
        if (!review.getStudent().getId().equals(studentId))
            throw ApiException.forbidden("Review belongs to another student");
        return review;
    }

    private void verifyTeacher(Lesson lesson, Long teacherId) {
        User teacher = users.findById(teacherId).orElseThrow(() -> ApiException.notFound("Teacher not found"));
        if (teacher.getRole() != Role.TEACHER || !lesson.getModule().getCourse().getTeacher().getId().equals(teacherId))
            throw ApiException.forbidden("Only the course teacher may perform this operation");
    }

    private void mustBeEnrolled(Long studentId, Long courseId) {
        if (!enrollments.existsByStudentIdAndCourseId(studentId, courseId))
            throw ApiException.badRequest("Student is not enrolled in this course");
    }

    private static EnrollmentView view(Enrollment e) {
        return new EnrollmentView(e.getId(), e.getCourse().getId(), e.getCourse().getTitle(),
                e.getStudent().getId(), e.getStudent().getName(), e.getEnrolledAt());
    }
    private static ReviewView view(CourseReview r) {
        return new ReviewView(r.getId(), r.getCourse().getId(), r.getStudent().getId(), r.getRating(), r.getComment());
    }
    private static AssignmentView view(Assignment a) {
        return new AssignmentView(a.getId(), a.getLesson().getId(), a.getTitle(), a.getDescription());
    }
    private static SubmissionView view(Submission s) {
        return new SubmissionView(s.getId(), s.getAssignment().getId(), s.getStudent().getId(), s.getAnswer(),
                s.getSubmittedAt(), s.getGrade(), s.getFeedback());
    }

    public record EnrollmentView(Long id, Long courseId, String courseTitle, Long studentId, String studentName, Instant enrolledAt) {}
    public record ReviewView(Long id, Long courseId, Long studentId, int rating, String comment) {}
    public record AssignmentView(Long id, Long lessonId, String title, String description) {}
    public record SubmissionView(Long id, Long assignmentId, Long studentId, String answer, Instant submittedAt,
                                 Integer grade, String feedback) {}
}
