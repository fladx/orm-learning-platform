package ru.vlad.orm.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QuizDto {
    private QuizDto() {}
    public record OptionInput(@NotBlank @Size(max = 250) String label, @NotNull Boolean correct) {}
    public record QuestionInput(@NotBlank @Size(max = 400) String prompt,
                                @NotNull @Size(min = 2, max = 12) List<@NotNull @Valid OptionInput> options) {}
    public record QuizInput(@NotNull @Positive Long teacherId, @NotBlank @Size(max = 150) String title,
                            @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid QuestionInput> questions) {}
    public record SubmissionInput(@NotNull @Positive Long studentId,
                                  @NotEmpty Map<@Positive Long, @NotNull @Positive Long> answers) {}
    public record OptionView(Long id, String label) {}
    public record QuestionView(Long id, String prompt, List<OptionView> options) {}
    public record QuizView(Long id, Long moduleId, String title, List<QuestionView> questions) {}
    public record Result(Long id, Long quizId, Long studentId, int score, int maxScore, Instant takenAt) {}
}
