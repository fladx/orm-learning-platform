package ru.vlad.orm.participation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.vlad.orm.catalog.CatalogService;
import ru.vlad.orm.catalog.Role;
import ru.vlad.orm.support.ApiException;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.vlad.orm.catalog.CatalogDto.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParticipationIntegrationTest {
    @Autowired CatalogService catalog;
    @Autowired ParticipationService service;
    @Autowired MockMvc mvc;

    long course, lesson, student, teacher;

    @BeforeEach
    void setUp() {
        student = newUser(Role.STUDENT);
        teacher = newUser(Role.TEACHER);
        long category = catalog.createCategory(new CategoryInput(UUID.randomUUID().toString())).id();
        course = catalog.createCourse(new CourseInput("ORM", null, category, teacher, Set.of())).id();
        long module = catalog.createModule(course, new ModuleInput("Mapping")).id();
        lesson = catalog.createLesson(module, new LessonInput("Relationships", "Content")).id();
    }

    @Test
    void enrollmentIsUniqueAndAppearsInBothListsUntilUnenrollment() throws Exception {
        String body = "{\"studentId\":" + student + "}";
        mvc.perform(post("/api/courses/{id}/enrollments", course).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.studentId").value(student));
        mvc.perform(post("/api/courses/{id}/enrollments", course).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        assertThat(service.courseEnrollments(course)).hasSize(1);
        assertThat(service.studentEnrollments(student)).hasSize(1);
        mvc.perform(delete("/api/courses/{course}/enrollments/{student}", course, student))
                .andExpect(status().isNoContent());
        assertThat(service.studentEnrollments(student)).isEmpty();
    }

    @Test
    void submissionRequiresEnrollmentAndAllowsOneGradedAttempt() {
        long assignment = service.createAssignment(lesson, teacher, "Exercise", "Model an association").id();
        assertThatThrownBy(() -> service.submit(assignment, student, "Answer")).isInstanceOf(ApiException.class);
        service.enroll(course, student);
        var submission = service.submit(assignment, student, "Answer");
        assertThat(submission.grade()).isNull();
        assertThatThrownBy(() -> service.submit(assignment, student, "Second answer")).isInstanceOf(ApiException.class);
        assertThat(service.grade(submission.id(), teacher, 91, "Good").grade()).isEqualTo(91);
        var graded = service.getSubmission(submission.id());
        assertThat(graded.answer()).isEqualTo(submission.answer());
        assertThat(java.time.Duration.between(graded.submittedAt(), submission.submittedAt()).abs())
                .isLessThan(java.time.Duration.ofMillis(1));
        assertThat(service.assignmentSubmissions(assignment)).hasSize(1);
        assertThat(service.studentSubmissions(student).getFirst().feedback()).isEqualTo("Good");
        assertThatThrownBy(() -> service.deleteAssignment(assignment, teacher)).isInstanceOf(ApiException.class);
    }

    @Test
    void unrelatedTeacherCannotEditAssignment() {
        long otherTeacher = newUser(Role.TEACHER);
        long assignment = service.createAssignment(lesson, teacher, "Exercise", "Description").id();
        assertThatThrownBy(() -> service.updateAssignment(assignment, otherTeacher, "Other", "New"))
                .isInstanceOf(ApiException.class);
        assertThat(service.updateAssignment(assignment, teacher, "Updated", "New").title()).isEqualTo("Updated");
        service.deleteAssignment(assignment, teacher);
        assertThat(service.lessonAssignments(lesson)).isEmpty();
    }

    @Test
    void reviewIsValidatedAndCanBeUpdatedOrDeleted() throws Exception {
        service.enroll(course, student);
        mvc.perform(post("/api/courses/{id}/reviews", course).contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":" + student + ",\"rating\":6,\"comment\":\"Bad rating\"}"))
                .andExpect(status().isBadRequest());
        var review = service.createReview(course, student, 4, "Good");
        assertThat(service.updateReview(review.id(), student, 5, "Great").rating()).isEqualTo(5);
        assertThat(service.courseReviews(course)).hasSize(1);
        service.deleteReview(review.id(), student);
        assertThat(service.courseReviews(course)).isEmpty();
    }

    @Test
    void unknownAssignmentReturnsNotFound() throws Exception {
        mvc.perform(get("/api/assignments/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private long newUser(Role role) {
        return catalog.createUser(new UserInput(role.name(), UUID.randomUUID() + "@example.test", role)).id();
    }
}
