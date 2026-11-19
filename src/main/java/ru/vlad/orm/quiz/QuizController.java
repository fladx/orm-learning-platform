package ru.vlad.orm.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static ru.vlad.orm.quiz.QuizDto.*;

@RestController
@RequestMapping("/api")
class QuizController {
    private final QuizService service;

    QuizController(QuizService service) { this.service = service; }

    @PostMapping("/modules/{moduleId}/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    QuizView create(@PathVariable @Positive Long moduleId, @RequestBody @Valid QuizInput input) {
        return service.create(moduleId, input);
    }

    @GetMapping("/quizzes")
    List<QuizView> list() { return service.list(); }

    @GetMapping("/quizzes/{quizId}")
    QuizView get(@PathVariable @Positive Long quizId) { return service.get(quizId); }

    @PutMapping("/quizzes/{quizId}")
    QuizView update(@PathVariable @Positive Long quizId, @RequestBody @Valid QuizInput input) {
        return service.update(quizId, input);
    }

    @DeleteMapping("/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable @Positive Long quizId, @RequestParam @Positive Long teacherId) {
        service.delete(quizId, teacherId);
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    Result submit(@PathVariable @Positive Long quizId, @RequestBody @Valid SubmissionInput input) {
        return service.submit(quizId, input);
    }

    @GetMapping("/quizzes/{quizId}/submissions")
    List<Result> byQuiz(@PathVariable @Positive Long quizId) { return service.byQuiz(quizId); }

    @GetMapping("/students/{studentId}/quiz-results")
    List<Result> byStudent(@PathVariable @Positive Long studentId) { return service.byStudent(studentId); }

    @GetMapping("/courses/{courseId}/quiz-results")
    List<Result> byCourse(@PathVariable @Positive Long courseId) { return service.byCourse(courseId); }

    @GetMapping("/modules/{moduleId}/quiz-results")
    List<Result> byModule(@PathVariable @Positive Long moduleId) { return service.byModule(moduleId); }
}
