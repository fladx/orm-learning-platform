package ru.vlad.orm.catalog;

import jakarta.validation.constraints.*;
import java.util.*;

public final class CatalogDto {
    private CatalogDto() {}
    public record UserInput(@NotBlank @Size(max=200) String name, @NotBlank @Email @Size(max=254) String email, @NotNull Role role) {}
    public record UserView(Long id, String name, String email, Role role) {}
    public record CategoryInput(@NotBlank @Size(max=200) String name) {}
    public record CategoryView(Long id, String name) {}
    public record TagInput(@NotBlank @Size(max=200) String name) {}
    public record TagView(Long id, String name) {}
    public record CourseInput(@NotBlank @Size(max=200) String title, @Size(max=2000) String description, @NotNull @Positive Long categoryId, @NotNull @Positive Long teacherId, @Size(max=100) Set<@NotNull @Positive Long> tagIds) {}
    public record CourseView(Long id, String title, String description, Long categoryId, Long teacherId, List<Long> tagIds) {}
    public record ModuleInput(@NotBlank @Size(max=200) String title) {}
    public record ModuleView(Long id, String title, Long courseId) {}
    public record LessonInput(@NotBlank @Size(max=200) String title, @Size(max=10000) String content) {}
    public record LessonView(Long id, String title, String content, Long moduleId) {}
    public record ProfileInput(@Size(max=2000) String bio) {}
    public record ProfileView(Long id, Long userId, String bio) {}
    public record CourseDetails(CourseView course, List<ModuleView> modules, List<LessonView> lessons) {}
}
