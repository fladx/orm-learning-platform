package ru.vlad.orm.quiz;

import org.junit.jupiter.api.BeforeEach;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.orm.catalog.CatalogService;
import ru.vlad.orm.catalog.Role;
import ru.vlad.orm.support.ApiException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static ru.vlad.orm.catalog.CatalogDto.*;
import static ru.vlad.orm.quiz.QuizDto.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizIntegrationTest {
    @Autowired CatalogService catalog;
    @Autowired QuizService quizzes;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    Long teacher, student, course, module;

    @BeforeEach
    void fixture() {
        String unique = UUID.randomUUID().toString();
        teacher = catalog.createUser(new UserInput("Teacher", unique + "@teacher.test", Role.TEACHER)).id();
        student = catalog.createUser(new UserInput("Student", unique + "@student.test", Role.STUDENT)).id();
        Long category = catalog.createCategory(new CategoryInput(unique)).id();
        course = catalog.createCourse(new CourseInput("Course", "", category, teacher, Set.of())).id();
        module = catalog.createModule(course, new ModuleInput("Module")).id();
    }

    QuizInput validQuiz() {
        return new QuizInput(teacher, "Quiz", List.of(
                new QuestionInput("Which is correct?", List.of(new OptionInput("A", true), new OptionInput("B", false)))));
    }

    @Test
    void createsExactlyOneQuizPerModule() {
        QuizView quiz = quizzes.create(module, validQuiz());
        assertThat(quizzes.get(quiz.id()).questions()).hasSize(1);
        assertThatThrownBy(() -> quizzes.create(module, validQuiz())).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsQuestionWithNoCorrectAnswer() {
        var wrong = new QuizInput(teacher, "Bad", List.of(
                new QuestionInput("Which?", List.of(new OptionInput("A", false), new OptionInput("B", false)))));
        assertThatThrownBy(() -> quizzes.create(module, wrong)).isInstanceOf(ApiException.class)
                .hasMessageContaining("exactly one correct answer");
    }

    @Test
    void requiresEnrollment() {
        QuizView quiz = quizzes.create(module, validQuiz());
        var q = quiz.questions().getFirst();
        assertThatThrownBy(() -> quizzes.submit(quiz.id(), new SubmissionInput(student,
                Map.of(q.id(), q.options().getFirst().id())))).isInstanceOf(ApiException.class)
                .hasMessageContaining("enrolled");
    }

    @Test
    void scoresAndPersistsOnceAndReturnsResultsThroughAllScopes() {
        jdbc.update("insert into enrollments(student_id, course_id, enrolled_at) values (?,?,?)",
                student, course, Timestamp.from(Instant.now()));
        QuizView quiz = quizzes.create(module, validQuiz());
        var q = quiz.questions().getFirst();
        var request = new SubmissionInput(student, Map.of(q.id(), q.options().getFirst().id()));
        Result result = quizzes.submit(quiz.id(), request);
        assertThat(result.score()).isEqualTo(1);
        assertThat(result.maxScore()).isEqualTo(1);
        em.clear();
        assertThat(quizzes.byStudent(student)).hasSize(1).first().satisfies(item -> {
            assertThat(item.id()).isEqualTo(result.id());
            assertThat(item.quizId()).isEqualTo(quiz.id());
        });
        assertThat(quizzes.byModule(module)).extracting(Result::id).containsExactly(result.id());
        assertThat(quizzes.byCourse(course)).extracting(Result::id).containsExactly(result.id());
        assertThatThrownBy(() -> quizzes.submit(quiz.id(), request)).isInstanceOf(ApiException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void updatesAndDeletesUnattemptedQuiz() {
        QuizView quiz = quizzes.create(module, validQuiz());
        var updated = quizzes.update(quiz.id(), new QuizInput(teacher, "Updated", validQuiz().questions()));
        assertThat(updated.title()).isEqualTo("Updated");
        quizzes.delete(quiz.id(), teacher);
        assertThatThrownBy(() -> quizzes.get(quiz.id())).isInstanceOf(ApiException.class);
    }
}
