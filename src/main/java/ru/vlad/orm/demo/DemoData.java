package ru.vlad.orm.demo;

import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.orm.catalog.CatalogDto.*;
import ru.vlad.orm.catalog.CatalogService;
import ru.vlad.orm.catalog.Role;
import ru.vlad.orm.catalog.UserRepository;
import ru.vlad.orm.participation.ParticipationService;
import ru.vlad.orm.quiz.QuizDto.*;
import ru.vlad.orm.quiz.QuizService;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoData implements ApplicationRunner {
    private final CatalogService catalog;
    private final UserRepository users;
    private final QuizService quizzes;
    private final ParticipationService participation;

    public DemoData(CatalogService catalog, UserRepository users, QuizService quizzes,
                    ParticipationService participation) {
        this.catalog = catalog;
        this.users = users;
        this.quizzes = quizzes;
        this.participation = participation;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() != 0) return;
        var teacher = catalog.createUser(new UserInput("Анна Смирнова", "teacher@example.test", Role.TEACHER));
        var student = catalog.createUser(new UserInput("Иван Соколов", "student@example.test", Role.STUDENT));
        catalog.putProfile(student.id(), new ProfileInput("Изучаю Java и базы данных"), true);
        var category = catalog.createCategory(new CategoryInput("Бэкенд-разработка"));
        var tag = catalog.createTag(new TagInput("Hibernate"));
        var course = catalog.createCourse(new CourseInput("Основы ORM",
                "Модель данных, транзакции и загрузка связей в Hibernate.", category.id(), teacher.id(), Set.of(tag.id())));
        var module = catalog.createModule(course.id(), new ModuleInput("Связи между сущностями"));
        var lesson = catalog.createLesson(module.id(),
                new LessonInput("Ленивая загрузка", "Посмотрите, когда Hibernate загружает связанные коллекции."));
        participation.enroll(course.id(), student.id());
        participation.createAssignment(lesson.id(), teacher.id(), "Схема связей",
                "Опишите модель курса и укажите кардинальности связей.");
        quizzes.create(module.id(), new QuizInput(teacher.id(), "Проверка по связям в JPA", List.of(
                new QuestionInput("Какая аннотация описывает связь многие-ко-многим?", List.of(
                        new OptionInput("@ManyToMany", true),
                        new OptionInput("@ManyToOne", false))))));
    }
}
