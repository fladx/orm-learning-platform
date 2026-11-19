package ru.vlad.orm.quiz;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.orm.catalog.*;
import ru.vlad.orm.catalog.Module;
import ru.vlad.orm.participation.EnrollmentRepository;
import ru.vlad.orm.support.ApiException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static ru.vlad.orm.quiz.QuizDto.*;

@Service
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizzes;
    private final QuizSubmissionRepository submissions;
    private final ModuleRepository modules;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    QuizService(QuizRepository quizzes, QuizSubmissionRepository submissions,
                ModuleRepository modules, UserRepository users, EnrollmentRepository enrollments) {
        this.quizzes = quizzes;
        this.submissions = submissions;
        this.modules = modules;
        this.users = users;
        this.enrollments = enrollments;
    }

    @Transactional
    public QuizView create(Long moduleId, QuizInput input) {
        Module module = modules.findById(moduleId).orElseThrow(() -> ApiException.notFound("Module not found"));
        requireTeacher(module, input.teacherId());
        if (quizzes.existsByModuleId(moduleId)) throw ApiException.conflict("Module already has a quiz");
        Quiz quiz = new Quiz();
        quiz.module = module;
        apply(quiz, input);
        return view(quizzes.saveAndFlush(quiz));
    }

    public QuizView get(Long id) {
        return view(quizzes.findById(id).orElseThrow(() -> ApiException.notFound("Quiz not found")));
    }

    public List<QuizView> list() {
        return quizzes.findAll().stream().map(QuizService::view).toList();
    }

    @Transactional
    public QuizView update(Long id, QuizInput input) {
        Quiz quiz = writable(id);
        requireTeacher(quiz.module, input.teacherId());
        requireNoResults(id);
        apply(quiz, input);
        return view(quizzes.saveAndFlush(quiz));
    }

    @Transactional
    public void delete(Long id, Long teacherId) {
        Quiz quiz = writable(id);
        requireTeacher(quiz.module, teacherId);
        requireNoResults(id);
        quizzes.delete(quiz);
    }

    @Transactional
    public Result submit(Long id, SubmissionInput input) {
        Quiz quiz = writable(id);
        User student = users.findById(input.studentId()).orElseThrow(() -> ApiException.notFound("Student not found"));
        if (student.getRole() != Role.STUDENT
                || !enrollments.existsByStudentIdAndCourseId(input.studentId(), quiz.module.getCourse().getId())) {
            throw ApiException.badRequest("Only enrolled students may take this quiz");
        }
        if (submissions.existsByQuizIdAndStudentId(id, input.studentId())) {
            throw ApiException.conflict("Quiz already submitted");
        }
        if (input.answers().size() != quiz.questions.size()) {
            throw ApiException.badRequest("Submit exactly one answer per question");
        }
        int score = 0;
        for (Question question : quiz.questions) {
            Long answerId = input.answers().get(question.id);
            AnswerOption selected = question.options.stream().filter(o -> Objects.equals(o.id, answerId))
                    .findFirst().orElseThrow(() -> ApiException.badRequest("Invalid or missing answer for question " + question.id));
            if (selected.correct) score++;
        }
        QuizSubmission submission = new QuizSubmission();
        submission.quiz = quiz;
        submission.student = student;
        submission.score = score;
        submission.maxScore = quiz.questions.size();
        submission.takenAt = Instant.now();
        return result(submissions.saveAndFlush(submission));
    }

    public List<Result> byStudent(Long id) { return results(submissions.findByStudentIdOrderById(id)); }
    public List<Result> byCourse(Long id) { return results(submissions.findByQuizModuleCourseIdOrderById(id)); }
    public List<Result> byModule(Long id) { return results(submissions.findByQuizModuleIdOrderById(id)); }
    public List<Result> byQuiz(Long id) { return results(submissions.findByQuizIdOrderById(id)); }

    private Quiz writable(Long id) {
        // A prior result query can leave a lazy Quiz proxy in this persistence context.
        // Field-based aggregate operations need the underlying managed instance.
        return Hibernate.unproxy(
                quizzes.findForWrite(id).orElseThrow(() -> ApiException.notFound("Quiz not found")), Quiz.class);
    }

    private void requireTeacher(Module module, Long teacherId) {
        User teacher = module.getCourse().getTeacher();
        if (teacher.getRole() != Role.TEACHER || !teacher.getId().equals(teacherId)) {
            throw ApiException.forbidden("Only the course teacher may manage its quizzes");
        }
    }

    private void requireNoResults(Long id) {
        if (submissions.existsByQuizId(id)) throw ApiException.conflict("Quiz with results cannot be edited or deleted");
    }

    private static void apply(Quiz quiz, QuizInput input) {
        quiz.title = input.title().trim();
        quiz.questions.clear();
        for (QuestionInput row : input.questions()) {
            if (row.options().stream().filter(o -> Boolean.TRUE.equals(o.correct())).count() != 1) {
                throw ApiException.badRequest("Each question must have exactly one correct answer");
            }
            Question question = new Question();
            question.quiz = quiz;
            question.prompt = row.prompt().trim();
            var labels = new HashSet<String>();
            for (OptionInput item : row.options()) {
                AnswerOption option = new AnswerOption();
                option.question = question;
                option.label = item.label().trim();
                if (!labels.add(option.label)) throw ApiException.badRequest("Answer options must be different");
                option.correct = item.correct();
                question.options.add(option);
            }
            quiz.questions.add(question);
        }
    }

    private static QuizView view(Quiz reference) {
        Quiz quiz = Hibernate.unproxy(reference, Quiz.class);
        return new QuizView(quiz.id, quiz.module.getId(), quiz.title, quiz.questions.stream()
                .map(q -> new QuestionView(q.id, q.prompt,
                        q.options.stream().map(o -> new OptionView(o.id, o.label)).toList())).toList());
    }

    private static List<Result> results(List<QuizSubmission> submissions) {
        return submissions.stream().map(QuizService::result).toList();
    }

    private static Result result(QuizSubmission s) {
        return new Result(s.id, s.quiz.getId(), s.student.getId(), s.score, s.maxScore, s.takenAt);
    }
}
